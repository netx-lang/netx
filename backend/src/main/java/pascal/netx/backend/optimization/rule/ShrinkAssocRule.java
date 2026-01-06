package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class ShrinkAssocRule extends UniRule {
    public ShrinkAssocRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case AND, OR, XOR, ADD, MUL -> new ShrinkContext(vertex).shrink();
            default -> false;
        };
    }

    private final class ShrinkContext {
        final Component origin;
        final List<Vertex> inputs;
        final List<List<Component>> candidates;
        final LinkedList<Vertex> chosen = new LinkedList<>();

        ShrinkContext(Component origin) {
            this.origin = origin;
            this.inputs = graph.inputsOf(origin);
            this.candidates = inputs.stream()
                    .map(graph::eqClassOf)
                    .map(eq -> findMatched(eq, origin.label()))
                    .toList();
        }

        boolean shrink() {
            if (candidates.stream().anyMatch(Predicate.not(List::isEmpty))) {
                shrink(0);
                markSeen(origin);
                return true;
            }
            return false;
        }

        private void shrink(int index) {
            if (index == candidates.size()) {
                var dual = graph.factory().component(origin.label(), origin.outWidth());
                for (var vertex : chosen) {
                    graph.addEdge(vertex, dual);
                }
                graph.union(origin, dual);
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
