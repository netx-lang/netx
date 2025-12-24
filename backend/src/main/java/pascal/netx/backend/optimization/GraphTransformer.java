package pascal.netx.backend.optimization;

import com.fasterxml.jackson.databind.JsonNode;
import pascal.netx.backend.AbstractNxonTransformer;
import pascal.netx.backend.Reference;
import pascal.netx.backend.ValueReference;
import pascal.netx.backend.WireReference;
import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.nxon.NxonType;
import pascal.netx.util.print.NotepadWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static pascal.netx.backend.NxonParseUtils.streamOf;

public final class GraphTransformer extends AbstractNxonTransformer {
    private final Graph graph = new Graph();

    GraphTransformer(NotepadWriter notepad, JsonNode json) {
        super(notepad, json);
    }

    private final Map<ValueReference, Wire> constDefs = new HashMap<>();
    private final Map<WireReference, Vertex> wireDefs = new HashMap<>();
    private final Map<JsonNode, Component> compDefs = new HashMap<>();

    private final Supplier<Integer> indexer = new AtomicInteger(0)::getAndIncrement;

    @Override
    public void parse() {
        buildWires();
        buildComponents();
        buildConnections();
    }

    public void rewrite() {
        new Rewriter(graph).rewrite();
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
                        var vertex = new Wire(indexer.get(), label);
                        wireDefs.put(ref, vertex);
                        graph.add(vertex);
                    }
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
                var vertex = new Component(indexer.get(), node.get("id").textValue());
                wireDefs.put(ref, vertex);
                compDefs.put(node, vertex);
                graph.add(vertex);
            }
        });
    }

    private void buildConnections() {
        iterate(json.get("design"), node -> {
            if (Objects.requireNonNull(NxonType.typeOf(node)) == NxonType.BUILTIN) {
                for (var input: node.get("input")) {
                    var vertex = compDefs.get(node);
                    switch (Reference.from(input.textValue())) {
                        case WireReference ref -> graph.addEdge(vertex, wireDefs.get(ref));
                        case ValueReference ref -> graph.addEdge(vertex, buildConstant(ref));
                    }
                }
            }
        });
    }

    private Wire buildConstant(ValueReference ref) {
        return constDefs.computeIfAbsent(ref, k -> {
            var vertex = new Wire(indexer.get(), ref.constant());
            graph.add(vertex);
            return vertex;
        });
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
