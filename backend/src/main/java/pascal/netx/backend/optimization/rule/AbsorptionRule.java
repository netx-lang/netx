package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class AbsorptionRule extends StatelessRule {
    public AbsorptionRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
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
            if (!(input instanceof Component c && c.label() == absorbLabel)) {
                direct.add(graph.eqRepOf(input));
            }
        }

        List<Component> absorbed = new ArrayList<>();
        for (var input : inputs) {
            if (input instanceof Component candidate && candidate.label() == absorbLabel) {
                if (containsAny(candidate, direct)) {
                    absorbed.add(candidate);
                }
            }
        }

        if (absorbed.isEmpty()) {
            return false;
        }

        inputs.removeAll(absorbed);
        dropOutgoing(parent, absorbed);
        cleanupAbsorbed(absorbed);
        return true;
    }

    private boolean containsAny(Component candidate, Set<Vertex> direct) {
        for (var in : graph.inputsOf(candidate)) {
            if (direct.contains(graph.eqRepOf(in))) {
                return true;
            }
        }
        return false;
    }

    private void dropOutgoing(Component parent, List<Component> absorbed) {
        for (var comp : absorbed) {
            graph.outputsOf(comp).remove(parent);
        }
    }

    private void cleanupAbsorbed(List<Component> absorbed) {
        for (var comp : absorbed) {
            if (graph.outputsOf(comp).isEmpty()) {
                graph.remove(comp);
            }
        }
    }
}
