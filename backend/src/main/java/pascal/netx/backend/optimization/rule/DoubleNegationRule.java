package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class DoubleNegationRule extends UniRule {
    public DoubleNegationRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (vertex.label() != Component.Label.NOT) {
            return false;
        }
        var neg = graph.inputsOf(vertex).getFirst();
        if (neg.contains(Component.class, hasLabel(Component.Label.NOT))) {
            var negIn = neg.filter(Component.class, hasLabel(Component.Label.NOT)).getFirst();
            var eq = graph.inputsOf(negIn).getFirst();
            graph.manager().replace(vertex, eq.getAny());
            return true;
        }
        return false;
    }
}
