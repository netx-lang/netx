package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;
import static pascal.hardscript.util.lang.BuiltinComponent.isBuiltin;

final class CompactDumper {
    private final GlobalScope globalScope;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ArrayNode design;
    private final SignatureManager manager = new SignatureManager();
    private final List<ObjectNode> definitions = new LinkedList<>();

    public CompactDumper(GlobalScope scope) {
        this.globalScope = scope;
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
        var top = globalScope.getTop();
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
            var parent = data.getParent().getLeastUpperComponent();
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

            dumpDefIO(definition, "input", signature.inputs());
            dumpDefIO(definition, "output", signature.outputs());

            var members = makeMembersIn(definition);
            for (var entity : component.getEntities()) {
                if (entity instanceof Data data) {
                    members.add(defineData(data));
                }
            }
            for (var child : component.getChildren()) {
                if (!(child instanceof ComponentScope sub)) {
                    throw new ShallNotReachHereError();
                }

                var instID = sub.getIdentifier();
                if (BuiltinComponent.isBuiltin(instID)) {
                    if (!NO_DUMP.contains(BuiltinComponent.of(instID))) {
                        members.add(instantiateBuiltin(sub, instID));
                    }
                } else {
                    members.add(instantiate(sub));
                }
            }
            return definition;
        }

        private void indexAllInstances() {
            allocateIndex(component);
            for (var child : component.getChildren()) {
                if (child instanceof ComponentScope childComponent) {
                    var id = childComponent.getIdentifier();
                    if (isBuiltin(id) && NO_DUMP.contains(BuiltinComponent.of(id))) {
                        continue;
                    }
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
            if (Objects.requireNonNull(BuiltinComponent.of(instID)) == BuiltinComponent.REGISTER) {
                dumpAttribute(builtin, sub);
            }
            List<Data> needDump = new LinkedList<>();
            for (var entity : sub.getEntities()) {
                if (entity instanceof Data data) {
                    needDump.add(data);
                }
            }
            if (!needDump.isEmpty()) {
                var subarray = makeMembersIn(builtin);
                needDump.forEach(c -> subarray.add(defineData(c)));
            }
            dumpInstIO(builtin, "input", sub.getInputs());
            dumpInstIO(builtin, "output", sub.getOutputs());
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

        private void dumpAttribute(ObjectNode node, ComponentScope component) {
            var attributes = createObjectNode();
            for (var entry : component.getAttributes().entrySet()) {
                attributes.put(entry.getKey().name(), dumpInstanceRef((Instance) entry.getValue()));
            }
            node.putIfAbsent("attribute", attributes);
        }

        private void dumpDefIO(ObjectNode node, String port, List<Instance> sub) {
            var array = makeArrayIn(node, port);
            for (Instance entity : sub) {
                String s = switch (entity) {
                    case Value value -> dumpValue(value);
                    case Data data -> data.getIdentifier().name();
                    case DataAccess access -> access.base().getIdentifier().name() + access.selector();
                };
                array.add(s);
            }
        }

        private void dumpInstIO(ObjectNode node, String port, List<Instance> sub) {
            var array = makeArrayIn(node, port);
            sub.stream().map(this::dumpInstanceRef).forEach(array::add);
        }

        private String dumpInstanceRef(Instance entity) {
            return switch (entity) {
                case Data data -> dumpDataRef(data);
                case Value value -> dumpValue(value);
                case DataAccess access -> dumpDataAccessRef(access);
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

        private String dumpDataAccessRef(DataAccess access) {
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
