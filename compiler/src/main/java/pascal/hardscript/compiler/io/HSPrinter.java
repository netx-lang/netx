package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.analysis.AliasAnalysis;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.components.Clock;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.Register;
import pascal.hardscript.compiler.model.components.Wire;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;

public final class HSPrinter {
    private final DesignScope design;
    private final ModuleInfo moduleInfo;
    private final AliasAnalysis.Result alias;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HSPrinter(DesignScope design) {
        this.design = design;
        this.moduleInfo = design.getModuleInfo();
        this.alias = new AliasAnalysis(design).analyze();
    }

    public ObjectNode dump() {
        return dump(design);
    }

    private ObjectNode dump(DesignScope design) {
        var result = createObjectNode();
        setType(result, "design");
        var members = makeMembersIn(result);
        design.getChildren().stream()
                .map(ModuleScope.class::cast)
                .map(this::dump).forEach(members::add);
        return result;
    }

    private ObjectNode dump(ModuleScope module) {
        var result = createObjectNode();
        setType(result, "module");
        setIdentifier(result, module.getIdentifier());

        var members = makeMembersIn(result);
        for (var entity : module.getEntities()) {
            if (entity instanceof Component component) {
                if (component.isUserDefined() || alias.sourceRepOf(component) == null) {
                    members.add(dump(component));
                }
            }
        }
        for (var sub : extractSubModules(module)) {
            var instID = sub.getIdentifier();
            if (moduleInfo.containsBuiltin(instID)) {
                if (!instID.string().equals("TO")) {
                    dumpBuiltinModule(sub, instID, members);
                }
            } else {
                members.add(dump(sub));
            }
        }
        return result;
    }

    private void dumpBuiltinModule(ModuleScope sub, Identifier instID, ArrayNode members) {
        var builtin = createObjectNode();
        setType(builtin, "builtin");
        setIdentifier(builtin, instID);
        dumpBuiltinPorts(builtin, "input", sub.getInputs());
        dumpBuiltinPorts(builtin, "output", sub.getOutputs());
        members.add(builtin);
    }

    private void dumpBuiltinPorts(ObjectNode builtin, String port, List<Component> sub) {
        var outputs = makeArrayIn(builtin, port);
        sub.stream().map(alias::repOf)
                .map(this::dumpIdentifierOf)
                .forEach(outputs::add);
    }

    private List<ModuleScope> extractSubModules(Scope module) {
        List<ModuleScope> result = new LinkedList<>();
        for (var subScope : module.getChildren()) {
            switch (subScope) {
                case ModuleScope sub -> result.add(sub);
                case BlockScope sub -> result.addAll(extractSubModules(sub));
                case DesignScope ignored -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    private ObjectNode dump(Component component) {
        return switch (component) {
            case Wire wire -> dumpWire(wire);
            case Clock clock -> dumpClock(clock);
            case Register register -> dumpRegister(register);
            default -> throw new ShallNotReachHereError();
        };
    }

    private ObjectNode dumpWire(Wire wire) {
        var result = createObjectNode();
        setIdentifier(result, wire);
        result.put("width", wire.width());
        setType(result, "wire");
        return result;
    }

    private ObjectNode dumpClock(Clock clock) {
        var result = createObjectNode();
        setIdentifier(result, clock);
        setType(result, "clock");
        return result;
    }

    private ObjectNode dumpRegister(Register register) {
        var result = createObjectNode();
        setIdentifier(result, register);
        result.put("width", register.width());
        setType(result, "register");
        return result;
    }

    private void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private String dumpIdentifierOf(Component component) {
        return component.getIdentifier().string() + "@" +
                Integer.toHexString(System.identityHashCode(component));
    }

    private void setIdentifier(ObjectNode node, Component component) {
        setIdentifier(node, dumpIdentifierOf(component));
    }

    private void setIdentifier(ObjectNode node, Identifier identifier) {
        setIdentifier(node, identifier.string());
    }

    private void setIdentifier(ObjectNode node, String value) {
        node.put("id", value);
    }

    private ArrayNode makeArrayIn(ObjectNode node, String key) {
        return node.putArray(key);
    }

    private ArrayNode makeMembersIn(ObjectNode node) {
        return makeArrayIn(node, "members");
    }

    private ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }
}
