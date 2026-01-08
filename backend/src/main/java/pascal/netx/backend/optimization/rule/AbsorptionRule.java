package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class AbsorptionRule extends UniRule {
    public AbsorptionRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case OR -> absorb(vertex, Component.Label.AND);
            case AND -> absorb(vertex, Component.Label.OR);
            default -> false;
        };
    }

    private boolean absorb(Component parent, Component.Label absorbLabel) {
        var inputs = graph.inputsOf(parent);
        Set<Graph.EqClass> direct = new HashSet<>();
        for (var input : inputs) {
            if (!input.contains(Component.class, hasLabel(absorbLabel))) {
                direct.add(input);
            }
        }

        List<Graph.EqClass> retained = new ArrayList<>();
        for (var input : inputs) {
            boolean canAbsorb = false;
            for (var eqIn : input.filter(Component.class, hasLabel(absorbLabel))) {
                if (graph.inputsOf(eqIn).stream().anyMatch(direct::contains)) {
                    canAbsorb = true;
                }
            }
            if (!canAbsorb) {
                retained.add(input);
            }
        }

        if (retained.size() == inputs.size()) {
            return false;
        }

        var dual = graph.factory().component(parent.label(), parent.outWidth());
        retained.forEach(cls -> graph.addEdge(cls, dual));
        graph.manager().replace(parent, dual);
        markSeen(parent);
        return true;
    }
}
