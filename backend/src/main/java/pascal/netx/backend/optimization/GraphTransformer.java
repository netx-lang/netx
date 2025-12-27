package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.backend.Reference;
import pascal.netx.backend.ValueReference;
import pascal.netx.backend.WireReference;
import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static pascal.netx.backend.NxonParseUtils.streamOf;

public final class GraphTransformer extends AbstractNxonTransformer {
    private final Graph graph = new Graph();

    GraphTransformer(NotepadWriter notepad, JsonNode json) {
        super(notepad, json);
    }

    private final Map<ValueReference, Constant> constRefs = new HashMap<>();
    private final Map<WireReference, Vertex> wireRefs = new HashMap<>();

    private final Map<WireReference, JsonNode> wireDefs = new HashMap<>();
    private final Map<JsonNode, Component> compDefs = new HashMap<>();

    @Override
    public void parse() {
        buildWires();
        buildComponents();
        buildConnections();
    }

    public void rewrite() {
        new Rewriter(graph).rewrite();
    }

    public Measurer.Metrics measure() {
        return new Measurer(graph).evaluate();
    }

    public void saturate(int areaWeight, int delayWeight) {
        var outputs = streamOf(json.get("design"), "output")
                .map(JsonNode::textValue).map(Reference::from)
                .map(WireReference.class::cast)
                .map(wireRefs::get)
                .collect(Collectors.toSet());
        new Saturator(graph, outputs, areaWeight, delayWeight).saturate();
    }

    private void buildWires() {
        var inputs = streamOf(json.get("design"), "input")
                .map(JsonNode::textValue)
                .map(Reference::from)
                .collect(Collectors.toSet());

        iterate(json.get("design"), node -> {
            switch (NxonType.typeOf(node)) {
                case WIRE, CLOCK -> {
                    var label = node.get("id").textValue();
                    var ref = (WireReference) Reference.from(label);
                    if (inputs.contains(ref)) {
                        var vertex = graph.newWire(label);
                        wireRefs.put(ref, vertex);
                    }
                    wireDefs.put(ref, node);
                }
                case BUILTIN, COMPONENT -> { /* ignored */ }
            }
        });
    }

    private void buildComponents() {
        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var output = node.get("output").get(0);
                var ref = (WireReference) Reference.from(output.textValue());

                var vertex = graph.newComponent(node.get("id").textValue());
                compDefs.put(node, vertex);
                if (!ref.selector().isEmpty()) {
                    buildWriteSelect(ref, vertex);
                } else {
                    wireRefs.put(ref, vertex);
                }
            }
        });
    }

    private void buildConnections() {
        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var vertex = compDefs.get(node);
                for (var input: node.get("input")) {
                    switch (Reference.from(input.textValue())) {
                        case WireReference ref -> {
                            if (wireRefs.containsKey(ref)) {
                                graph.addEdge(wireRefs.get(ref), vertex);
                            } else {
                                buildReadSelect(ref, vertex);
                            }
                        }
                        case ValueReference ref -> graph.addEdge(buildConstant(ref), vertex);
                    }
                }
            }
        });
    }

    private void buildWriteSelect(WireReference ref, Component vertex) {
        var wire = buildWireWithSelector(ref);
        var select = buildSelect(ref);
        graph.addEdge(select, wire);
        graph.addEdge(vertex, select);
        graph.union(vertex, select);
    }

    private void buildReadSelect(WireReference ref, Component vertex) {
        var wire = buildWireWithSelector(ref);
        var select = buildSelect(ref);
        graph.addEdge(wire, select);
        graph.addEdge(select, vertex);
        wireRefs.put(ref, select);
    }

    private Vertex buildWireWithSelector(WireReference ref) {
        return wireRefs.computeIfAbsent(
                ref.withoutSelector(), key -> graph.newWire(wireDefs.get(key).get("id").textValue()));
    }

    private Select buildSelect(WireReference ref) {
        String literal = ref.selector().substring(1, ref.selector().length() - 1);
        if (literal.contains(":")) {
            var array = literal.split(":");
            int msb = Integer.parseInt(array[0]);
            int lsb = Integer.parseInt(array[1]);
            return graph.newSelect(msb, lsb);
        } else {
            int index = Integer.parseInt(literal);
            return graph.newSelect(index, index);
        }
    }

    private Constant buildConstant(ValueReference ref) {
        return constRefs.computeIfAbsent(ref, k -> graph.newConstant(BitVector.parse(k.constant())));
    }

    public void dump() {
        graph.dump(notepad);
    }

    private void iterate(JsonNode node, Consumer<JsonNode> action) {
        action.accept(node);
        if (node.has("members")) {
            for (var member : node.get("members")) {
                iterate(member, action);
            }
        }
    }
}
