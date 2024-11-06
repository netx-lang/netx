package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.*;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;

public final class HSPrinter {
    private final DesignScope design;
    private final ComponentInfo componentInfo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public HSPrinter(DesignScope design) {
        this.design = design;
        this.componentInfo = design.getComponentInfo();
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

    private ObjectNode dump(ComponentScope module) {
        var result = createObjectNode();
        setType(result, "component");
        setIdentifier(result, module.getIdentifier());

        dumpComponentIO(result, "input", module.getInputs());
        dumpComponentIO(result, "output", module.getOutputs());
        var members = makeMembersIn(result);
        for (var entity : module.getEntities()) {
            if (entity instanceof Data data) {
                members.add(dump(data));
            }
        }
        for (var sub : extractSubComponent(module)) {
            var instID = sub.getIdentifier();
            if (componentInfo.containsBuiltin(instID)) {
                if (!NO_DUMP.contains(BuiltinComponent.of(instID.string()))) {
                    dumpBuiltinComponent(sub, instID, members);
                }
            } else {
                members.add(dump(sub));
            }
        }
        dumpComponentAttribute(result, module);
        return result;
    }

    private void dumpBuiltinComponent(ComponentScope sub, Identifier instID, ArrayNode members) {
        var builtin = createObjectNode();
        setType(builtin, "builtin");
        setIdentifier(builtin, instID);

        List<Data> needDump = new LinkedList<>();
        for (var entity : sub.getEntities()) {
            if (entity instanceof Data data) {
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

    private void dumpComponentIO(ObjectNode builtin, String port, List<Instance> sub) {
        var outputs = makeArrayIn(builtin, port);
        sub.stream().map(this::dumpInstance).forEach(outputs::add);
    }

    private void dumpComponentAttribute(ObjectNode node, ComponentScope module) {
        if (!module.getAttributes().isEmpty()) {
            var attributes = createObjectNode();
            for (var entry : module.getAttributes().entrySet()) {
                attributes.put(entry.getKey(), dumpInstance((Instance) entry.getValue()));
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
        switch (wire.getType()) {
            case BitType type -> result.put("width", type.width());
            case PolyType ignored -> {
            }
        }
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

    private String dumpInstance(Instance entity) {
        return switch (entity) {
            case Value value -> dumpValue(value);
            case Data data -> dumpData(data);
        };
    }

    private String dumpValue(Value value) {
        return switch (value) {
            case BooleanValue v -> String.valueOf(v.value());
            case Int32Value v -> String.valueOf(v.value());
            case BitVectorValue v -> v.value().toString();
        };
    }

    private String dumpData(Data data) {
        var sourceLoc = Objects.requireNonNullElse(
                data.getSourceLoc(),
                data.getUpperComponent().getSourceLoc());
        if (data instanceof DataAccess access) {
            return dumpDataAccess(access);
        } else {
            return String.format("%s@(%s)", data.getIdentifier().string(), sourceLoc);
        }
    }

    private String dumpDataAccess(DataAccess access) {
        return switch (access.base()) {
            case DataAccess base -> dumpDataAccess(base) + access.selector();
            case Value value -> dumpValue(value) + access.selector();
            case Data data -> dumpData(data) + access.selector();
        };
    }

    private void setIdentifier(ObjectNode node, Instance inst) {
        setIdentifier(node, dumpInstance(inst));
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
