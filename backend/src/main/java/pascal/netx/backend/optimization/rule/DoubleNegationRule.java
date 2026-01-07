package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class DoubleNegationRule extends UniRule {
    public DoubleNegationRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (vertex.label() != Component.Label.NOT || hasSeen(vertex)) {
            return false;
        }
        var neg = graph.inputsOf(vertex).getFirst();
        var only = neg.stream()
                .filter(Component.class::isInstance)
                .map(Component.class::cast)
                .filter(comp -> comp.label() == Component.Label.NOT)
                .findAny();

        if (only.isPresent()) {
            graph.union(vertex, only.get());
            markSeen(vertex);
            return true;
        }
        return false;
    }
}
