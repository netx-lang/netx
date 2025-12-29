package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.*;

public final class DistributivityRule extends UniMemRule {
    public DistributivityRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case ADD -> pull(vertex, Component.Label.MUL);
            case AND -> pull(vertex, Component.Label.OR);
            case OR -> pull(vertex, Component.Label.AND);
            default -> false;
        };
    }

    private boolean pull(Component vertex, Component.Label over) {
        if (!allInMatch(vertex, over)) {
            return false;
        }

        var common = commonParts(graph.inputsOf(vertex));
        if (common.isEmpty()) {
            return false;
        }

        int width = vertex.outWidth();
        var dual = graph.newComponent(over, width);
        common.forEach(elem -> graph.addEdge(elem, dual));
        for (var in : graph.inputsOf(vertex)) {
            var mul = (Component) in;
            var remainder = graph.newComponent(vertex.label(), width);
            for (var mulIn : graph.inputsOf(mul)) {
                if (!common.contains(mulIn)) {
                    graph.addEdge(mulIn, remainder);
                }
            }
            graph.addEdge(remainder, dual);
        }
        graph.union(vertex, dual);
        markSeen(vertex);
        return true;
    }

    private Set<Vertex> commonParts(List<Vertex> components) {
        var result = new HashSet<>(graph.inputsOf(components.getFirst()));
        for (int i = 1; i < components.size(); i++) {
            result.retainAll(graph.inputsOf(components.get(i)));
        }
        return result;
    }

    private boolean allInMatch(Component vertex, Component.Label label) {
        var inputs = graph.inputsOf(vertex);
        return inputs.size() >= 2 && inputs.stream().allMatch(
                in -> in instanceof Component c && c.label() == label
        );
    }
}
