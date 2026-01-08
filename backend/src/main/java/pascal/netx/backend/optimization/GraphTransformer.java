package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.backend.Reference;
import pascal.netx.backend.ValueReference;
import pascal.netx.backend.WireReference;
import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        graph.setInputs(extractIOVertices(streamOf(json.get("design"), "input")));
        graph.setOutputs(extractIOVertices(streamOf(json.get("design"), "output")));
    }

    private List<Vertex> extractIOVertices(Stream<JsonNode> stream) {
        return stream.map(JsonNode::textValue)
                .map(Reference::from)
                .map(WireReference.class::cast)
                .map(wireRefs::get)
                .toList();
    }

    public void rewrite() {
        new Rewriter(graph).rewrite();
    }

    public Measurer.Metrics measure() {
        return new Measurer(graph).evaluate();
    }

    public void saturate(int areaWeight, int delayWeight) {
        new Saturator(graph, areaWeight, delayWeight).saturate();
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
                        int width = definedWithOf(node);
                        var vertex = graph.factory().wire(label, width);
                        wireRefs.put(ref, vertex);
                    }
                    wireDefs.put(ref, node);
                }
                case BUILTIN, COMPONENT -> { /* ignored */ }
            }
        });
    }

    private static int definedWithOf(JsonNode node) {
        int width = 1;
        if (node.has("width")) {
            width = node.get("width").intValue();
        }
        return width;
    }

    private void buildComponents() {
        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var output = node.get("output").get(0);
                var ref = (WireReference) Reference.from(output.textValue());
                int width;
                if (wireDefs.containsKey(ref)) {
                    width = wireDefs.get(ref).get("width").intValue();
                } else {
                    width = parseSelector(ref.selector()).width();
                }

                String label = node.get("id").textValue();
                var vertex = graph.factory().component(label, width);
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
        for (var entry : definedSelects.entrySet()) {
            var wireRef = entry.getKey();
            var selects = entry.getValue();
            selects.sort(Comparator.comparing(WriteSelect::selector).reversed());
            int width = wireDefs.get(wireRef).get("width").intValue();
            var concat = graph.factory().component(Component.Label.CONCAT, width);
            for (var select : selects) {
                graph.addEdge(select, concat);
            }
            wireRefs.put(wireRef, concat);
        }

        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                var vertex = compDefs.get(node);
                boolean isEq = node.get("id").textValue().equals("TO");
                for (var input: node.get("input")) {
                    var inVertex = switch (Reference.from(input.textValue())) {
                        case WireReference ref -> {
                            if (wireRefs.containsKey(ref)) {
                                var result = wireRefs.get(ref);
                                graph.addEdge(result, vertex);
                                yield result;
                            } else {
                                var base = Objects.requireNonNull(wireRefs.get(ref.withoutSelector()));
                                Selector selector = parseSelector(ref.selector());
                                var select = graph.factory().readSelect(selector);
                                graph.addEdge(base, select);
                                graph.addEdge(select, vertex);
                                wireRefs.put(ref, select);
                                yield select;
                            }
                        }
                        case ValueReference ref -> {
                            var constant = buildConstant(ref);
                            graph.addEdge(constant, vertex);
                            yield constant;
                        }
                    };
                    if (isEq) {
                        graph.replace(vertex, inVertex);
                    }
                }
            }
        });
    }

    private final Map<WireReference, List<WriteSelect>> definedSelects = new HashMap<>();

    private void buildWriteSelect(WireReference ref, Component vertex) {
        var key = ref.withoutSelector();
        if (wireRefs.containsKey(key)) {
            throw new ShallNotReachHereError();
        }

        var label = wireDefs.get(key).get("id").textValue();
        Selector selector = parseSelector(ref.selector());
        var select = graph.factory().writeSelect(label, selector);
        definedSelects.computeIfAbsent(key, _ -> new LinkedList<>()).add(select);
        graph.addEdge(vertex, select);
    }

    Selector parseSelector(String selector) {
        String literal = selector.substring(1, selector.length() - 1);
        if (literal.contains(":")) {
            var array = literal.split(":");
            int msb = Integer.parseInt(array[0]);
            int lsb = Integer.parseInt(array[1]);
            return new Selector(msb, lsb);
        } else {
            int index = Integer.parseInt(literal);
            return new Selector(index, index);
        }
    }

    private Constant buildConstant(ValueReference ref) {
        return constRefs.computeIfAbsent(ref, k -> {
            BitVector value = BitVector.parse(k.constant());
            return graph.factory().constant(value);
        });
    }

    public void dump() {
        graph.dump(notepad);
        notepad.flush();
        notepad.close();
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
