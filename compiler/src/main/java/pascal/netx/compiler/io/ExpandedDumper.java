package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.model.instance.*;
import pascal.netx.compiler.model.instance.type.BitType;
import pascal.netx.compiler.model.instance.type.PolyType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.GlobalScope;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import java.util.*;

import static pascal.netx.util.lang.BuiltinComponent.NO_DUMP;

final class ExpandedDumper {
    private final GlobalScope design;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Data, Integer> dataIndices = new HashMap<>();

    public ExpandedDumper(GlobalScope scope) {
        this.design = scope;
    }

    private static void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private static void setDefID(ObjectNode node, Identifier id) {
        node.put("id", id.name());
        node.put("loc", id.getSourceLoc().toString());
    }

    private static void setBuiltinID(ObjectNode node, Identifier id) {
        node.put("id", id.name());
        node.put("loc", id.getSourceLoc().toString());
    }

    private static ArrayNode makeArrayIn(ObjectNode node, String key) {
        return node.putArray(key);
    }

    private static ArrayNode makeMembersIn(ObjectNode node) {
        return makeArrayIn(node, "members");
    }

    public ObjectNode dump() {
        var result = createObjectNode();
        var node = dumperOf(design.getTop()).dump();
        result.put("totalData", dataIndices.size());
        result.putIfAbsent("design", node);
        return result;
    }

    private ComponentDumper dumperOf(ComponentScope scope) {
        return new ComponentDumper(scope);
    }

    private ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    private class ComponentDumper {
        private final ComponentScope component;

        private ComponentDumper(ComponentScope component) {
            this.component = component;
        }

        public ObjectNode dump() {
            var result = createObjectNode();
            setType(result, "component");
            setDefID(result, component.getIdentifier());

            dumpInstIO(result, "input", component.getInputs());
            dumpInstIO(result, "output", component.getOutputs());

            List<ObjectNode> needDump = new LinkedList<>();
            for (var entity : component.getEntities()) {
                if (entity instanceof Data data) {
                    needDump.add(defineData(data));
                }
            }
            for (var child : component.getChildren()) {
                var instID = child.getIdentifier();
                if (BuiltinComponent.isBuiltin(instID)) {
                    if (!NO_DUMP.contains(BuiltinComponent.of(instID))) {
                        needDump.add(instantiateBuiltin(child, instID));
                    }
                } else if (!child.isInterface()) {
                    needDump.add(dumperOf(child).dump());
                }
            }
            if (!needDump.isEmpty()) {
                var members = makeMembersIn(result);
                needDump.forEach(members::add);
            }
            return result;
        }

        private ObjectNode instantiateBuiltin(ComponentScope sub, Identifier instID) {
            var builtin = createObjectNode();
            setType(builtin, "builtin");
            setBuiltinID(builtin, instID);
            dumpInstIO(builtin, "input", sub.getInputs());
            dumpInstIO(builtin, "output", sub.getOutputs());
            if (Objects.requireNonNull(BuiltinComponent.of(instID)) == BuiltinComponent.REGISTER) {
                var attributes = createObjectNode();
                for (var entry : sub.getAttributes().entrySet()) {
                    attributes.put(entry.getKey().name(), dumpInstance((Instance) entry.getValue()));
                }
                builtin.putIfAbsent("attribute", attributes);
            }
            List<Data> needDump = sub.getEntities().stream()
                    .filter(Data.class::isInstance).map(Data.class::cast).toList();
            if (!needDump.isEmpty()) {
                var subarray = makeMembersIn(builtin);
                needDump.forEach(c -> subarray.add(defineData(c)));
            }
            return builtin;
        }

        private ObjectNode defineData(Data data) {
            return switch (data) {
                case Wire wire -> defineWire(wire);
                case Clock clock -> defineClock(clock);
            };
        }

        private ObjectNode defineWire(Wire wire) {
            var result = createObjectNode();
            setType(result, "wire");
            switch (wire.getType()) {
                case BitType type -> result.put("width", type.width());
                case PolyType ignored -> throw new ShallNotReachHereError();
                default -> {
                    // do nothing for ArrayType and InterfaceType
                }
            }
            setDataID(result, wire);
            return result;
        }

        private ObjectNode defineClock(Clock clock) {
            var result = createObjectNode();
            setType(result, "clock");
            setDataID(result, clock);
            return result;
        }

        private void dumpInstIO(ObjectNode node, String port, List<Instance> sub) {
            var array = makeArrayIn(node, port);
            sub.stream().map(this::dumpInstance).forEach(array::add);
        }

        private String dumpInstance(Instance entity) {
            return switch (entity) {
                case Data data -> dumpData(data);
                case Value value -> dumpValue(value);
                case DataAccess access -> dumpDataAccess(access);
                default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
            };
        }

        private String dumpData(Data data) {
            return String.format("%s@%d", data.getIdentifier().name(), indexOf(data));
        }

        private int indexOf(Data data) {
            return dataIndices.computeIfAbsent(data, _ -> dataIndices.size());
        }

        private String dumpValue(Value value) {
            return switch (value) {
                case BooleanValue v -> String.valueOf(v.value());
                case Int32Value v -> String.valueOf(v.value());
                case BitVectorValue v -> v.value().toString();
            };
        }

        private String dumpDataAccess(DataAccess access) {
            return dumpData(access.base()) + access.selector();
        }

        private void setDataID(ObjectNode node, Data data) {
            node.put("id", dumpData(data));
            node.put("loc", data.getSourceLoc().toString());
        }
    }
}
