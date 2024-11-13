package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.BlockScope;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;
import static pascal.hardscript.util.lang.BuiltinComponent.REGISTER;

public final class HSPrinter {
    private int dataCount = 0;
    private final DesignScope designScope;
    private final Set<Signature> signatures = new HashSet<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ObjectNode design;
    private final List<ObjectNode> definitions = new ArrayList<>();

    public HSPrinter(DesignScope scope) {
        this.designScope = scope;
        this.design = createObjectNode();
    }

    public ObjectNode dump() {
        var top = designScope.getTop();
        signatures.add(top.getSignature());
        definitions.add(defineComponent(top.getSignature(), top));
        design.put("top", top.getIdentifier().name());
        design.put("dataCount", dataCount + 1);
        Collections.reverse(definitions);
        makeArrayIn(design, "definitions").addAll(definitions);
        return design;
    }

    private ObjectNode defineComponent(Signature signature, ComponentScope component) {
        var definition = createObjectNode();
        setType(definition, "definition");
        setIdentifier(definition, component.getIdentifier());

        dumpComponentIO(definition, "input", signature.inputs());
        dumpComponentIO(definition, "output", signature.outputs());
        var members = makeMembersIn(definition);
        for (var entity : component.getEntities()) {
            if (entity instanceof Data data) {
                members.add(dump(data));
            }
        }
        for (var sub : extractSubComponent(component)) {
            var instID = sub.getIdentifier();
            if (BuiltinComponent.isBuiltin(instID)) {
                if (!NO_DUMP.contains(BuiltinComponent.of(instID.name()))) {
                    dumpBuiltinComponent(sub, instID, members);
                }
            } else {
                members.add(dump(sub));
            }
        }
        return definition;
    }

    private ObjectNode dump(ComponentScope component) {
        var signature = component.getSignature();
        var result = createObjectNode();
        if (!signatures.contains(signature)) {
            signatures.add(signature);
            definitions.add(defineComponent(signature, component));
        }
        result.put("type", "component");
        result.put("id", component.getIdentifier().name());
        var members = makeMembersIn(result);
        for (var entity : component.getEntities()) {
            if (entity instanceof Data data) {
                members.add(dump(data));
            }
        }
        dumpComponentIO(result, "input", component.getInputs());
        dumpComponentIO(result, "output", component.getOutputs());
        return result;
    }

    private void dumpBuiltinComponent(ComponentScope sub, Identifier instID, ArrayNode members) {
        var builtin = createObjectNode();
        if (BuiltinComponent.of(instID) == REGISTER) {
            setType(builtin, "register");
            dumpComponentAttribute(builtin, sub);
        } else {
            setType(builtin, "component");
            setIdentifier(builtin, instID);
        }
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

        members.add(builtin);
    }

    private void dumpComponentIO(ObjectNode builtin, String port, List<Instance> sub) {
        var outputs = makeArrayIn(builtin, port);
        sub.stream().map(this::dumpInstance).forEach(outputs::add);
    }

    private void dumpComponentAttribute(ObjectNode node, ComponentScope component) {
        if (!component.getAttributes().isEmpty()) {
            var attributes = createObjectNode();
            for (var entry : component.getAttributes().entrySet()) {
                attributes.put(entry.getKey().name(), dumpInstance(entry.getValue()));
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
            case Data data -> dumpData(data);
            case Value value -> dumpValue(value);
            case DataAccess access -> dumpDataAccess(access);
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
        dataCount = Math.max(dataCount, data.getIndex());
        return data.getIdentifier().toString() + "@" + data.getIndex();
    }

    private String dumpDataAccess(DataAccess access) {
        return dumpData(access.base()) + access.selector();
    }

    private void setIdentifier(ObjectNode node, Instance inst) {
        setIdentifier(node, dumpInstance(inst));
    }

    private void setIdentifier(ObjectNode node, Identifier identifier) {
        setIdentifier(node, identifier.name());
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
