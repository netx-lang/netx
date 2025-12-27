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
        if (neg instanceof Component comp && comp.label() == Component.Label.NOT) {
            var only = graph.inputsOf(neg).getFirst();
            graph.union(vertex, only);
            markSeen(vertex);
            return true;
        }
        return false;
    }
}
