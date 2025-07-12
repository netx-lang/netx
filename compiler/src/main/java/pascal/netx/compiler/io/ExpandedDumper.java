package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.model.scope.Attributes;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.*;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static pascal.netx.util.lang.BuiltinComponent.ATTRIBUTED;

final class ExpandedDumper {
    private final PackageScope design;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Data, Integer> dataIndices = new HashMap<>();

    public ExpandedDumper(PackageScope scope) {
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
        var design_node = dumperOf(design.getTop()).dump();

        result.put("totalData", dataIndices.size());
        var input = makeArrayIn(result, "top_input");
        var output = makeArrayIn(result, "top_output");
        var signature = design.getTop().getSignature();
        signature.inputs().stream().map(this::dumpTopPort).forEach(input::add);
        signature.outputs().stream().map(this::dumpTopPort).forEach(output::add);
        result.putIfAbsent("design", design_node);
        return result;
    }

    private String dumpTopPort(Variable variable) {
        return switch (variable) {
            case Data data -> data.getIdentifier().name();
            case Value value -> value.toString();
            case Access access -> dumpTopPort(access.base()) + access.selector();
            default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
        };
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
            if (component.hasAttributes()) {
                dumpInstAttr(result, component.getAttributes());
            }

            List<ObjectNode> needDump = new LinkedList<>();
            component.getBinding().forEachData(data -> needDump.add(defineData(data)));
            component.getChildren().stream()
                    .map(ComponentScope.class::cast)
                    .forEach(child -> {
                        var instID = child.getIdentifier();
                        if (BuiltinComponent.isBuiltin(instID)) {
                            needDump.add(instantiateBuiltin(child, instID));
                        } else if (!child.isInterface()) {
                            needDump.add(dumperOf(child).dump());
                        }
                    });
            component.getAliases().stream()
                    .map(this::dumpAlias)
                    .forEach(needDump::add);
            if (!needDump.isEmpty()) {
                var members = makeMembersIn(result);
                needDump.forEach(members::add);
            }
            return result;
        }

        private ObjectNode dumpAlias(ComponentScope.Alias alias) {
            var result = createObjectNode();
            setType(result, "builtin");
            setDefID(result, Identifier.of("TO"));
            dumpInstIO(result, "input", List.of(alias.rhs()));
            dumpInstIO(result, "output", List.of(alias.lhs()));
            return result;
        }

        private ObjectNode instantiateBuiltin(ComponentScope sub, Identifier instID) {
            var builtin = createObjectNode();
            setType(builtin, "builtin");
            setBuiltinID(builtin, instID);
            dumpInstIO(builtin, "input", sub.getInputs());
            dumpInstIO(builtin, "output", sub.getOutputs());
            if (ATTRIBUTED.contains(BuiltinComponent.of(instID))) {
                dumpInstAttr(builtin, sub.getAttributes());
            }
            List<Data> needDump = new LinkedList<>();
            sub.getBinding().forEachData(needDump::add);
            if (!needDump.isEmpty()) {
                var subarray = makeMembersIn(builtin);
                needDump.forEach(c -> subarray.add(defineData(c)));
            }
            return builtin;
        }

        private void dumpInstAttr(ObjectNode node, Attributes attributes) {
            var result = createObjectNode();
            for (var entry : attributes.entrySet()) {
                if (entry.getValue() instanceof Variable variable) {
                    result.put(entry.getKey().name(), dumpVariable(variable));
                } else {
                    result.put(entry.getKey().name(), entry.getValue().toString());
                }
            }
            node.putIfAbsent("attribute", result);
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

        private void dumpInstIO(ObjectNode node, String port, List<Variable> sub) {
            var array = makeArrayIn(node, port);
            sub.stream().map(this::dumpVariable).forEach(array::add);
        }

        private String dumpVariable(Variable entity) {
            return switch (entity) {
                case Data data -> dumpData(data);
                case Value value -> dumpValue(value);
                case Access access -> dumpDataAccess(access);
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

        private String dumpDataAccess(Access access) {
            return dumpData(access.base()) + access.selector();
        }

        private void setDataID(ObjectNode node, Data data) {
            node.put("id", dumpData(data));
            node.put("loc", data.getSourceLoc().toString());
        }
    }
}
