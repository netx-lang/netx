package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.model.Signature;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pascal.netx.util.lang.BuiltinComponent.ATTRIBUTED;

final class CompactDumper {
    private final PackageScope packageScope;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ArrayNode design;
    private final SignatureManager manager = new SignatureManager();
    private final List<ObjectNode> definitions = new LinkedList<>();

    public CompactDumper(PackageScope scope) {
        this.packageScope = scope;
        this.design = objectMapper.createArrayNode();
    }

    private static void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private static void setDefID(ObjectNode node, Identifier id, int defIndex) {
        node.put("id", id.name());
        node.put("defIndex", defIndex);
        node.put("loc", id.getSourceLoc().toString());
    }

    private static void setDataID(ObjectNode node, Identifier id) {
        node.put("id", id.name());
        node.put("loc", id.getSourceLoc().toString());
    }

    private static ArrayNode makeArrayIn(ObjectNode node, String key) {
        return node.putArray(key);
    }

    private static ArrayNode makeMembersIn(ObjectNode node) {
        return makeArrayIn(node, "members");
    }

    public ArrayNode dump() {
        var top = packageScope.getTop();
        definitions.addFirst(dumperOf(top).dump());
        definitions.forEach(design::add);
        return design;
    }

    private ComponentDumper dumperOf(ComponentScope scope) {
        Signature sign = scope.getSignature();
        return new ComponentDumper(manager.addSignature(sign), sign, scope);
    }

    private ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    private static class SignatureManager {
        private final Map<Signature, Integer> signatures = new HashMap<>();
        private final Map<String, Integer> idCount = new HashMap<>();

        public int getCount(Signature signature) {
            return signatures.get(signature);
        }

        public boolean contains(Signature signature) {
            return signatures.containsKey(signature);
        }

        public int addSignature(Signature signature) {
            var id = signature.caller().getIdentifier();
            int count = idCount.getOrDefault(id.name(), 0) + 1;
            idCount.put(id.name(), count);
            signatures.put(signature, count);
            return count;
        }
    }

    private class ComponentDumper {
        private final int index;
        private final Signature signature;
        private final ComponentScope component;
        private final Supplier<Integer> indexer = new AtomicInteger(0)::getAndIncrement;
        private final Map<ComponentScope, Integer> indexMap = new HashMap<>();

        private ComponentDumper(int index, Signature signature, ComponentScope component) {
            this.index = index;
            this.signature = signature;
            this.component = component;
        }

        private int indexOf(Data data) {
            var parent = data.getParent();
            if (indexMap.containsKey(parent)) {
                return indexMap.get(parent);
            }
            return 0;
        }

        public ObjectNode dump() {
            indexAllInstances();

            var definition = createObjectNode();
            setType(definition, "component");
            setDefID(definition, signature.caller().getDefinition().getIdentifier(), index);

            var attributes = signature.caller().getArgList();
            if (attributes != null && !attributes.isEmpty()) {
                var parameters = signature.caller().getDefinition().getAttributes();
                var arguments = attributes.getArguments();
                ObjectNode binding = createObjectNode();
                for (int i = 0; i < parameters.size(); i++) {
                    binding.put(parameters.get(i).name(), arguments.get(i).toString());
                }
                definition.set("attribute", binding);
            }

            dumpInstIO(definition, "input", signature.inputs());
            dumpInstIO(definition, "output", signature.outputs());

            List<ObjectNode> needDump = new LinkedList<>();
            component.getBinding().forEachData(data -> needDump.add(defineData(data)));
            component.getChildren().stream()
                    .map(ComponentScope.class::cast)
                    .forEach(child -> {
                        var instID = child.getIdentifier();
                        if (BuiltinComponent.isBuiltin(instID)) {
                            needDump.add(instantiateBuiltin(child, instID));
                        } else if (!child.isInterface()) {
                            needDump.add(instantiate(child));
                        }
                    });
            component.getAliases().stream()
                    .map(this::dumpAlias)
                    .forEach(needDump::add);
            if (!needDump.isEmpty()) {
                var members = makeMembersIn(definition);
                needDump.forEach(members::add);
            }
            return definition;
        }

        private ObjectNode dumpAlias(ComponentScope.Alias alias) {
            var result = createObjectNode();
            setType(result, "builtin");
            result.put("id", String.format("TO@%d", indexer.get()));
            result.put("defIndex", manager.getCount(signature));
            dumpInstIO(result, "input", List.of(alias.rhs()));
            dumpInstIO(result, "output", List.of(alias.lhs()));
            return result;
        }

        private void indexAllInstances() {
            allocateIndex(component);
            for (var child : component.getChildren()) {
                if (child instanceof ComponentScope childComponent) {
                    allocateIndex(childComponent);
                }
            }
        }

        private void allocateIndex(ComponentScope component) {
            var index = indexer.get();
            indexMap.put(component, index);
        }

        private ObjectNode instantiate(ComponentScope component) {
            var signature = component.getSignature();
            ObjectNode result = createObjectNode();
            if (!manager.contains(signature)) {
                definitions.addFirst(dumperOf(component).dump());
            }
            setType(result, "instantiate");
            setInstID(result, component);
            dumpInstIO(result, "input", component.getInputs());
            dumpInstIO(result, "output", component.getOutputs());
            return result;
        }

        private ObjectNode instantiateBuiltin(ComponentScope sub, Identifier instID) {
            var builtin = createObjectNode();
            setType(builtin, "builtin");
            setBuiltinID(builtin, sub);
            dumpInstIO(builtin, "input", sub.getInputs());
            dumpInstIO(builtin, "output", sub.getOutputs());

            if (ATTRIBUTED.contains(BuiltinComponent.of(instID))) {
                var attributes = createObjectNode();
                for (var entry : sub.getAttributes().entrySet()) {
                    attributes.put(entry.getKey().name(), dumpVariableRef((Variable) entry.getValue()));
                }
                builtin.putIfAbsent("attribute", attributes);
            }
            List<Data> needDump = new LinkedList<>();
            sub.getBinding().forEachData(needDump::add);
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
            setDataID(result, wire.getIdentifier());
            return result;
        }

        private ObjectNode defineClock(Clock clock) {
            var result = createObjectNode();
            setType(result, "clock");
            setDataID(result, clock.getIdentifier());
            return result;
        }

        private void dumpInstIO(ObjectNode node, String port, List<Variable> sub) {
            var array = makeArrayIn(node, port);
            sub.stream().map(this::dumpVariableRef).forEach(array::add);
        }

        private String dumpVariableRef(Variable entity) {
            return switch (entity) {
                case Data data -> dumpDataRef(data);
                case Value value -> dumpValue(value);
                case Access access -> dumpDataAccessRef(access);
                default -> throw new ShallNotReachHereError("Should be eliminated by CompositeDataBlaster");
            };
        }

        private String dumpDataRef(Data data) {
            return String.format("%s@%d", data.getIdentifier().name(), indexOf(data));
        }

        private String dumpValue(Value value) {
            return switch (value) {
                case BooleanValue v -> String.valueOf(v.value());
                case Int32Value v -> String.valueOf(v.value());
                case BitVectorValue v -> v.value().toString();
            };
        }

        private String dumpDataAccessRef(Access access) {
            return dumpDataRef(access.base()) + access.selector();
        }

        private void setInstID(ObjectNode node, ComponentScope component) {
            var id = component.getIdentifier();
            node.put("id", String.format("%s@%d", id.name(), indexMap.get(component)));
            node.put("defIndex", manager.getCount(component.getSignature()));
            node.put("loc", id.getSourceLoc().toString());
        }

        private void setBuiltinID(ObjectNode node, ComponentScope component) {
            Identifier id = component.getIdentifier();
            int instIndex = indexMap.get(component);
            node.put("id", String.format("%s@%d", id.name(), instIndex));
            node.put("loc", id.getSourceLoc().toString());
        }
    }
}
