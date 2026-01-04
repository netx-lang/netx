package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class IdempotentRule extends UniMemRule {
    public IdempotentRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case AND, OR, NAND, NOR -> {
                var inputs = graph.inputsOf(vertex);
                var seen = inputs.stream().map(graph::eqRepOf).distinct().toList();
                if (seen.size() < inputs.size()) {
                    var dual = graph.factory().component(vertex.label(), vertex.outWidth());
                    seen.stream()
                            .map(graph::eqClassOf)
                            .map(Graph.EqClass::getAny)
                            .forEach(v -> graph.addEdge(v, dual));
                    graph.union(vertex, dual);
                    markSeen(vertex);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }
}
