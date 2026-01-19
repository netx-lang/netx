package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class ShrinkAssocRule extends UniRule {
    public ShrinkAssocRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        return switch (vertex.label()) {
            case AND, OR, XOR, ADD, MUL -> new ShrinkContext(vertex).shrink();
            default -> false;
        };
    }

    private final class ShrinkContext {
        final Component origin;
        final List<Graph.EqClass> inputs;
        final List<List<Component>> candidates;
        final LinkedList<Graph.EqClass> chosen = new LinkedList<>();

        ShrinkContext(Component origin) {
            this.origin = origin;
            this.inputs = graph.inputsOf(origin);
            this.candidates = inputs.stream()
                    .map(eq -> findMatched(eq, origin.label()))
                    .toList();
        }

        boolean shrink() {
            if (candidates.stream().anyMatch(Predicate.not(List::isEmpty))) {
                shrink(0);
                return true;
            }
            return false;
        }

        private void shrink(int index) {
            if (index == candidates.size()) {
                var dual = graph.factory().copy(origin);
                for (var vertex : chosen) {
                    graph.addEdge(vertex, dual);
                }
                graph.manager().replace(origin, dual);
            } else {
                if (candidates.get(index).isEmpty()) {
                    chosen.add(inputs.get(index));
                    shrink(index + 1);
                    chosen.removeLast();
                } else {
                    for (var candidate : candidates.get(index)) {
                        var toShrink = graph.inputsOf(candidate);
                        chosen.addAll(toShrink);
                        shrink(index + 1);
                        for (int i = 0; i < toShrink.size(); i++) {
                            chosen.removeLast();
                        }
                    }
                }
            }
        }
    }

    private List<Component> findMatched(Graph.EqClass eqClass, Component.Label label) {
        return eqClass.stream()
                .filter(Component.class::isInstance)
                .map(Component.class::cast)
                .filter(comp -> comp.label() == label)
                .toList();
    }
}
