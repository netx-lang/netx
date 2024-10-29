package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.analysis.AliasAnalysis;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.components.Clock;
import pascal.hardscript.compiler.model.components.Component;
import pascal.hardscript.compiler.model.components.Register;
import pascal.hardscript.compiler.model.components.Wire;
import pascal.hardscript.compiler.model.scope.BlockScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.ModuleScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;

public final class HSPrinter {
    private final DesignScope design;
    private final AliasAnalysis.Result alias;

    public HSPrinter(DesignScope design) {
        this.design = design;
        this.alias = new AliasAnalysis(design).analyze();
    }

    public JsonNode dump() {
        return dump(design);
    }

    private JsonNode dump(DesignScope design) {
        var result = new ObjectMapper().createObjectNode();
        setType(result, "design");
        var members = makeMembersIn(result);
        design.getChildren().stream()
                .map(ModuleScope.class::cast)
                .map(this::dump).forEach(members::add);
        return result;
    }

    private JsonNode dump(ModuleScope module) {
        var result = new ObjectMapper().createObjectNode();
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
        for (var instantiation : extractSubModules(module)) {
            members.add(dump(instantiation));
        }
        return result;
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

    private JsonNode dump(Component component) {
        return switch (component) {
            case Wire wire -> dumpWire(wire);
            case Clock clock -> dumpClock(clock);
            case Register register -> dumpRegister(register);
            default -> throw new ShallNotReachHereError();
        };
    }

    private JsonNode dumpWire(Wire wire) {
        var result = new ObjectMapper().createObjectNode();
        setIdentifier(result, wire.getIdentifier());
        result.put("width", wire.width());
        setType(result, "wire");
        return result;
    }

    private JsonNode dumpClock(Clock clock) {
        var result = new ObjectMapper().createObjectNode();
        setIdentifier(result, clock.getIdentifier());
        setType(result, "clock");
        return result;
    }

    private JsonNode dumpRegister(Register register) {
        var result = new ObjectMapper().createObjectNode();
        setIdentifier(result, register.getIdentifier());
        result.put("width", register.width());
        setType(result, "register");
        return result;
    }

    private void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private void setIdentifier(ObjectNode node, Identifier identifier) {
        node.put("id", identifier.string());
    }

    private ArrayNode makeMembersIn(ObjectNode node) {
        return node.putArray("members");
    }
}
