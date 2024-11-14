package pascal.hardscript.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.hardscript.compiler.model.component.Signature;
import pascal.hardscript.compiler.model.instance.*;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.DesignScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static pascal.hardscript.util.lang.BuiltinComponent.NO_DUMP;
import static pascal.hardscript.util.lang.BuiltinComponent.REGISTER;

public final class HSDumper {
    private final DesignScope designScope;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final ArrayNode design;
    private final SignatureManager manager = new SignatureManager();
    private final List<ObjectNode> definitions = new LinkedList<>();

    public HSDumper(DesignScope scope) {
        this.designScope = scope;
        this.design = objectMapper.createArrayNode();
    }

    public ArrayNode dump() {
        var top = designScope.getTop();
        var signIndex = manager.addSignature(top.getSignature());
        definitions.addFirst(
                new ComponentDumper(signIndex, top.getSignature(), top).dump());
        definitions.forEach(design::add);
        return design;
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

        private int allocateIndex(ComponentScope component) {
            var index = indexer.get();
            indexMap.put(component, index);
            return index;
        }

        private int indexOf(Data data) {
            var parent = data.getParent().getLeastUpperComponent();
            if (indexMap.containsKey(parent))
                return indexMap.get(data.getParent().getLeastUpperComponent());
            return 0;
        }

        public ObjectNode dump() {
            allocateIndex(component);

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

        private ObjectNode instantiate(ComponentScope component) {
            var signature = component.getSignature();
            var result = createObjectNode();
            if (!manager.contains(signature)) {
                definitions.addFirst(new ComponentDumper(manager.addSignature(signature), signature, component).dump());
            }
            setType(result, "instantiate");
            setInstID(result, component.getIdentifier(), manager.getCount(signature), allocateIndex(component));
            dumpInstIO(result, "input", component.getInputs());
            dumpInstIO(result, "output", component.getOutputs());
            return result;
        }

        private ObjectNode instantiateBuiltin(ComponentScope sub, Identifier instID) {
            var builtin = createObjectNode();
            if (BuiltinComponent.of(instID) == REGISTER) {
                setType(builtin, "register");
                dumpComponentAttribute(builtin, sub);
            } else {
                setType(builtin, "builtin");
                setBuiltinID(builtin, instID, allocateIndex(sub));
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

        private void dumpComponentAttribute(ObjectNode node, ComponentScope component) {
            if (component.getAttributes() != null && !component.getAttributes().isEmpty()) {
                var attributes = createObjectNode();
                for (var entry : component.getAttributes().entrySet()) {
                    attributes.put(entry.getKey().name(), dumpInstanceRef(entry.getValue()));
                }
                node.putIfAbsent("attribute", attributes);
            }
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
    }

    private static void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private static void setDefID(ObjectNode node, Identifier id, int defIndex) {
        node.put("id", id.name());
        node.put("defIndex", defIndex);
        node.put("loc", id.getSourceLoc().toString());
    }


    private static void setInstID(ObjectNode node, Identifier id, int defIndex, int instIndex) {
        node.put("id", String.format("%s@%d", id.name(), instIndex));
        node.put("defIndex", defIndex);
        node.put("loc", id.getSourceLoc().toString());
    }

    private static void setBuiltinID(ObjectNode node, Identifier id, int instIndex) {
        node.put("id", String.format("%s@%d", id.name(), instIndex));
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
}
