package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

import java.util.*;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class DistributivityRule extends UniRule {
    public DistributivityRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case ADD -> apply(vertex, Component.Label.MUL);
            case AND -> apply(vertex, Component.Label.OR);
            case OR -> apply(vertex, Component.Label.AND);
            default -> false;
        };
    }

    private boolean apply(Component vertex, Component.Label over) {
        if (!applicable(vertex, over)) {
            return false;
        }

        var candidates = graph.inputsOf(vertex).stream()
                .map(cls -> cls.filter(Component.class, hasLabel(over)))
                .toList();
        return new DistributivityContext(vertex, over, candidates).distribute();
    }

    class DistributivityContext {
        private final Component vertex;
        private final Component.Label over;
        private final List<List<Component>> candidates;

        boolean changed = false;
        private final Set<Graph.EqClass> common = new HashSet<>();
        private final List<Component> selected = new LinkedList<>();

        DistributivityContext(Component vertex, Component.Label over, List<List<Component>> candidates) {
            this.vertex = vertex;
            this.over = over;
            this.candidates = candidates;
        }

        boolean distribute() {
            distribute(0);
            return changed;
        }

        void distribute(int index) {
            if (index == candidates.size()) {
                int width = vertex.outWidth();
                var dual = graph.factory().component(over, width);
                common.forEach(elem -> graph.addEdge(elem.getAny(), dual));
                for (var in : selected) {
                    var remainder = graph.factory().component(vertex.label(), width);
                    for (var mulIn : graph.inputsOf(in)) {
                        if (!common.contains(mulIn)) {
                            graph.addEdge(mulIn, remainder);
                        }
                    }
                    graph.addEdge(remainder, dual);
                }
                graph.union(vertex, dual);
                markSeen(vertex);
                changed = true;
            } else {
                for (var candidate : candidates.get(index)) {
                    var inputs = graph.inputsOf(candidate);
                    if (common.isEmpty()) {
                        common.addAll(inputs);
                    } else {
                        common.retainAll(inputs);
                    }

                    if (!common.isEmpty()) {
                        selected.add(candidate);
                        distribute(index + 1);
                        selected.removeLast();
                    }
                }
            }
        }

    }

    private boolean applicable(Component vertex, Component.Label label) {
        var inputs = graph.inputsOf(vertex);
        return inputs.size() >= 2 && inputs.stream()
                .allMatch(cls -> cls.contains(Component.class, hasLabel(label)));
    }
}
