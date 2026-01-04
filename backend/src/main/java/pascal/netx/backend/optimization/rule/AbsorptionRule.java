package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.*;

public final class AbsorptionRule extends UniMemRule {
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
        Set<Vertex> direct = new HashSet<>();
        for (var input : inputs) {
            if (findInEqClass(input, absorbLabel).isEmpty()) {
                direct.add(graph.eqRepOf(input));
            }
        }

        List<Component> absorbed = new ArrayList<>();
        for (var input : inputs) {
            var optional = findInEqClass(input, absorbLabel);
            if (optional.isPresent()) {
                var candidate = optional.get();
                if (containsAny(candidate, direct)) {
                    absorbed.add(candidate);
                }
            }
        }

        if (absorbed.isEmpty()) {
            return false;
        }

        var dual = graph.factory().component(parent.label(), parent.outWidth());
        absorbed.forEach(vertex -> direct.addAll(graph.inputsOf(vertex)));
        direct.forEach(vertex -> graph.addEdge(vertex, dual));
        graph.union(parent, dual);
        markSeen(parent);
        return true;
    }

    private Optional<Component> findInEqClass(Vertex vertex, Component.Label label) {
        return graph.eqClassOf(vertex).stream()
                .filter(Component.class::isInstance)
                .map(Component.class::cast)
                .filter(comp -> comp.label() == label)
                .findAny();
    }

    private boolean containsAny(Component candidate, Set<Vertex> direct) {
        for (var in : graph.inputsOf(candidate)) {
            if (direct.contains(graph.eqRepOf(in))) {
                return true;
            }
        }
        return false;
    }
}
