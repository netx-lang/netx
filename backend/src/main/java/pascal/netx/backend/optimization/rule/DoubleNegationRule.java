package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class DoubleNegationRule extends UniMemRule {
    public DoubleNegationRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (vertex.label() != Component.Label.NOT || hasSeen(vertex)) {
            return false;
        }
        var neg = graph.inputsOf(vertex).getFirst();
        var only = graph.eqClassOf(neg).stream()
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
