package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class IdempotentRule extends UniRule {
    public IdempotentRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        return switch (vertex.label()) {
            case AND, OR, NAND, NOR -> {
                var inputs = graph.inputsOf(vertex);
                var seen = inputs.stream().distinct().toList();
                if (seen.size() < inputs.size()) {
                    var dual = graph.factory().component(vertex.label(), vertex.outWidth());
                    seen.forEach(v -> graph.addEdge(v, dual));
                    graph.manager().replace(vertex, dual);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }
}
