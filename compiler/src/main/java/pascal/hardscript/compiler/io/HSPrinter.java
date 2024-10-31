package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.analysis.AliasAnalysis;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.data.Clock;
import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataAccess;
import pascal.hardscript.compiler.model.data.Wire;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.compiler.model.value.BitVectorValue;
import pascal.hardscript.compiler.model.value.BooleanValue;
import pascal.hardscript.compiler.model.value.Int32Value;
import pascal.hardscript.compiler.model.value.Value;
import pascal.hardscript.util.exception.ShallNotReachHereError;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class HSPrinter {
    private final DesignScope design;
    private final ComponentInfo componentInfo;
    private final AliasAnalysis.Result alias;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HSPrinter(DesignScope design) {
        this.design = design;
        this.componentInfo = design.getComponentInfo();
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
                .map(ComponentScope.class::cast)
                .map(this::dump).forEach(members::add);
        return result;
    }

    private static final Set<String> NO_DUMP = Set.of("SPLIT", "TO");

    private ObjectNode dump(ComponentScope module) {
        var result = createObjectNode();
        setType(result, "module");
        setIdentifier(result, module.getIdentifier());

        dumpComponentIO(result, "input", module.getInputs());
        dumpComponentIO(result, "output", module.getOutputs());
        var members = makeMembersIn(result);
        for (var entity : module.getEntities()) {
            if (entity instanceof Data data && needDump(data)) {
                members.add(dump(data));
            }
        }
        for (var sub : extractSubComponent(module)) {
            var instID = sub.getIdentifier();
            if (componentInfo.containsBuiltin(instID)) {
                if (!NO_DUMP.contains(instID.string())) {
                    dumpBuiltinComponent(sub, instID, members);
                }
            } else {
                members.add(dump(sub));
            }
        }
        dumpComponentAttribute(result, module);
        return result;
    }

    private boolean needDump(Data data) {
        return (data.isUserDefined()
                && alias.repOf(data) == data
        ) || alias.isBuiltinRep(data);
    }

    private void dumpBuiltinComponent(ComponentScope sub, Identifier instID, ArrayNode members) {
        var builtin = createObjectNode();
        setType(builtin, "builtin");
        setIdentifier(builtin, instID);

        List<Data> needDump = new LinkedList<>();
        for (var entity : sub.getEntities()) {
            if (entity instanceof Data data && needDump(data)) {
                needDump.add(data);
            }
        }
        if (!needDump.isEmpty()) {
            var subarray = makeMembersIn(builtin);
            needDump.forEach(c -> subarray.add(dump(c)));
        }
        dumpComponentIO(builtin, "input", sub.getInputs());
        dumpComponentIO(builtin, "output", sub.getOutputs());
        dumpComponentAttribute(builtin, sub);
        members.add(builtin);
    }

    private void dumpComponentIO(ObjectNode builtin, String port, List<Data> sub) {
        var outputs = makeArrayIn(builtin, port);
        sub.stream().map(alias::repOf)
                .map(this::dumpIdentifierOf)
                .forEach(outputs::add);
    }

    private void dumpComponentAttribute(ObjectNode node, ComponentScope module) {
        if (!module.getAttributes().isEmpty()) {
            var attributes = createObjectNode();
            for (var entry : module.getAttributes().entrySet()) {
                attributes.put(entry.getKey(), dumpIdentifierOf(entry.getValue()));
            }
            node.putIfAbsent("attribute", attributes);
        }
    }

    private List<ComponentScope> extractSubComponent(Scope module) {
        List<ComponentScope> result = new LinkedList<>();
        for (var subScope : module.getChildren()) {
            switch (subScope) {
                case ComponentScope sub -> result.add(sub);
                case BlockScope sub -> result.addAll(extractSubComponent(sub));
                case DesignScope ignored -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    private ObjectNode dump(Data data) {
        return switch (data) {
            case Wire wire -> dumpWire(wire);
            case Clock clock -> dumpClock(clock);
            default -> throw new ShallNotReachHereError();
        };
    }

    private ObjectNode dumpWire(Wire wire) {
        var result = createObjectNode();
        setType(result, "wire");
        setIdentifier(result, wire);
        result.put("width", wire.width());
        return result;
    }

    private ObjectNode dumpClock(Clock clock) {
        var result = createObjectNode();
        setType(result, "clock");
        setIdentifier(result, clock);
        return result;
    }

    private void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private String dumpIdentifierOf(Entity entity) {
        return switch (entity) {
            case Data data -> dumpIdentifierOf(data);
            case Value value -> dumpIdentifierOf(value);
            default -> throw new ShallNotReachHereError();
        };
    }

    private String dumpIdentifierOf(Data data) {
        var sourceLoc = Objects.requireNonNullElse(
                data.getSourceLoc(),
                data.getUpperComponent().getSourceLoc());

        if (data instanceof DataAccess access) {
            return String.format("%s@(%s)%s", data.getIdentifier().string(), sourceLoc, access.selector());

        } else {
            return String.format("%s@(%s)", data.getIdentifier().string(), sourceLoc);
        }
    }

    private String dumpIdentifierOf(Value value) {
        return switch (value) {
            case BooleanValue v -> String.valueOf(v.value());
            case Int32Value v -> String.valueOf(v.value());
            case BitVectorValue v -> v.value().toString();
        };
    }

    private void setIdentifier(ObjectNode node, Data data) {
        setIdentifier(node, dumpIdentifierOf(data));
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
