package pascal.netx.compiler.io;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import pascal.netx.compiler.model.circuit.scope.Attributes;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.Scope;
import pascal.netx.compiler.model.circuit.signal.*;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Hole;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.*;

import static pascal.netx.util.lang.BuiltinComponent.ATTRIBUTED;

public final class NxonDumper {
    private final PackageScope design;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private final Map<Wire, Integer> indexer = new HashMap<>();

    public NxonDumper(PackageScope scope) {
        this.design = scope;
    }

    private static void setType(ObjectNode node, String type) {
        node.put("type", type);
    }

    private static void setDefID(ObjectNode node, Identifier id, SourceLoc loc) {
        node.put("id", id.name());
        node.put("loc", loc.toString());
    }

    private static void setBuiltinID(ObjectNode node, Identifier id, SourceLoc loc) {
        node.put("id", id.name());
        node.put("loc", loc.toString());
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

        result.put("total", indexer.size());
        var input = makeArrayIn(result, "top_input");
        var output = makeArrayIn(result, "top_output");
        var signature = design.getTop().getSignature();
        signature.inputs().stream().map(this::dumpTopPort).forEach(input::add);
        signature.outputs().stream().map(this::dumpTopPort).forEach(output::add);
        result.putIfAbsent("design", design_node);
        return result;
    }

    private String dumpTopPort(Signal signal) {
        return switch (signal) {
            case Wire wire -> wire.getIdentifier().name();
            case Value value -> value.toString();
            case Access access -> dumpTopPort(access.base()) + access.selector();
            default -> throw new ShallNotReachHereError("Should be eliminated by CompositionBlaster");
        };
    }

    private ComponentDumper dumperOf(ComponentScope scope) {
        return new ComponentDumper(scope);
    }

    private ObjectNode createObjectNode() {
        return objectMapper.createObjectNode();
    }

    static private Collection<Wire> getDefinedWiresIn(ComponentScope scope) {
        var wires = new ArrayList<Wire>();
        scope.forEachEntity(entity -> {
            if (entity instanceof Wire wire && isDefinedIn(scope, wire)) {
                wires.add(wire);
            }
        });
        return wires;
    }

    static private boolean isDefinedIn(ComponentScope component, Wire wire) {
        Scope scope = wire.getParent();
        while (scope != null) {
            if (scope.equals(component)) {
                return true;
            }
            scope = scope.getParent();
        }
        return false;
    }

    private class ComponentDumper {
        private final ComponentScope component;

        private ComponentDumper(ComponentScope component) {
            this.component = component;
        }

        public ObjectNode dump() {
            var result = createObjectNode();
            setType(result, "component");
            setDefID(result, component.getIdentifier(), component.getSourceLoc());

            dumpInstIO(result, "input", component.getInputs());
            dumpInstIO(result, "output", component.getOutputs());
            if (component.hasAttributes()) {
                dumpInstAttr(result, component.getAttributes());
            }

            List<ObjectNode> needDump = new LinkedList<>();
            getDefinedWiresIn(component).forEach(wire -> needDump.add(defineWire(wire)));
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
            setDefID(result, Identifier.of("TO"), component.getSourceLoc());
            dumpInstIO(result, "input", List.of(alias.rhs()));
            dumpInstIO(result, "output", List.of(alias.lhs()));
            return result;
        }

        private ObjectNode instantiateBuiltin(ComponentScope sub, Identifier instID) {
            var builtin = createObjectNode();
            setType(builtin, "builtin");
            setBuiltinID(builtin, instID, sub.getSourceLoc());
            dumpInstIO(builtin, "input", sub.getInputs());
            dumpInstIO(builtin, "output", sub.getOutputs());
            if (ATTRIBUTED.contains(BuiltinComponent.of(instID))) {
                dumpInstAttr(builtin, sub.getAttributes());
            }
            List<Wire> needDump = new LinkedList<>(getDefinedWiresIn(sub));
            if (!needDump.isEmpty()) {
                var subarray = makeMembersIn(builtin);
                needDump.forEach(c -> subarray.add(defineWire(c)));
            }
            return builtin;
        }

        private void dumpInstAttr(ObjectNode node, Attributes attributes) {
            var result = createObjectNode();
            for (var entry : attributes.entrySet()) {
                if (entry.getValue() instanceof Signal signal) {
                    result.put(entry.getKey().name(), dumpVariable(signal));
                } else {
                    result.put(entry.getKey().name(), entry.getValue().toString());
                }
            }
            node.putIfAbsent("attribute", result);
        }

        private ObjectNode defineWire(Wire wire) {
            if (wire instanceof Clock clock) {
                return defineClock(clock);
            }

            var result = createObjectNode();
            setType(result, "wire");
            switch (wire.getType()) {
                case BitType type -> result.put("width", type.width());
                case Hole ignored -> throw new ShallNotReachHereError();
                default -> {
                    // do nothing for ArrayType and InterfaceType
                }
            }
            setWireID(result, wire);
            return result;
        }

        private ObjectNode defineClock(Clock clock) {
            var result = createObjectNode();
            setType(result, "clock");
            setWireID(result, clock);
            return result;
        }

        private void dumpInstIO(ObjectNode node, String port, List<Signal> sub) {
            var array = makeArrayIn(node, port);
            sub.stream().map(this::dumpVariable).forEach(array::add);
        }

        private String dumpVariable(Signal entity) {
            return switch (entity) {
                case Wire wire -> dumpWire(wire);
                case Value value -> dumpValue(value);
                case Access access -> dumpAccess(access);
                default -> throw new ShallNotReachHereError("Should be eliminated by CompositionBlaster");
            };
        }

        private String dumpWire(Wire wire) {
            return String.format("%s@%d", wire.getIdentifier().name(), indexOf(wire));
        }

        private int indexOf(Wire data) {
            return indexer.computeIfAbsent(data, _ -> indexer.size());
        }

        private String dumpValue(Value value) {
            return value.toString();
        }

        private String dumpAccess(Access access) {
            return dumpWire(access.base()) + access.selector();
        }

        private void setWireID(ObjectNode node, Wire wire) {
            node.put("id", dumpWire(wire));
            node.put("loc", wire.getSourceLoc().toString());
        }
    }
}
