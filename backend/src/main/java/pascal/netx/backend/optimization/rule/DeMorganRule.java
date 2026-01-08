package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.LinkedList;
import java.util.List;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class DeMorganRule extends BidiRule {
    public DeMorganRule(Graph graph) {
        super(graph);
    }

    @Override
    boolean push(Component vertex) {
        if (hasPushed(vertex)) {
            return false;
        }

        int width = vertex.outWidth();
        switch (vertex.label()) {
            case NOT -> {
                var input = graph.inputsOf(vertex).getFirst();
                if (input.contains(Component.class, hasLabel(Component.Label.AND))) {
                    for (var comp : input.filter(Component.class, hasLabel(Component.Label.AND))) {
                        var orComp = graph.factory().component(Component.Label.OR, width);
                        pushDeMorgan(vertex, comp, orComp);
                    }
                    return true;
                }
                if (input.contains(Component.class, hasLabel(Component.Label.OR))) {
                    for (var comp : input.filter(Component.class, hasLabel(Component.Label.OR))) {
                        var andComp = graph.factory().component(Component.Label.AND, width);
                        pushDeMorgan(vertex, comp, andComp);
                    }
                    return true;
                }
            }
            case NOR -> {
                var andComp = graph.factory().component(Component.Label.AND, width);
                pushDeMorgan(vertex, vertex, andComp);
                return true;
            }
            case NAND ->  {
                var orComp = graph.factory().component(Component.Label.OR, width);
                pushDeMorgan(vertex, vertex, orComp);
                return true;
            }
        }
        return false;
    }

    private void pushDeMorgan(Component vertex, Component origin, Component dual) {
        for (var in : graph.inputsOf(origin)) {
            var notIn = graph.factory().component(Component.Label.NOT, in.width());
            graph.addEdge(in, notIn);
            graph.addEdge(notIn, dual);
        }
        graph.manager().union(vertex, dual);
        markPushed(vertex);
        markPulled(dual);
    }

    @Override
    boolean pull(Component vertex) {
        if (hasPulled(vertex)) {
            return false;
        }

        if (isPullable(vertex)) {
            switch (vertex.label()) {
                case AND -> {
                    pullDeMorgan(vertex, Component.Label.NOR);
                    return true;
                }
                case OR -> {
                    pullDeMorgan(vertex, Component.Label.NAND);
                    return true;
                }
            }
        }
        return false;
    }

    private void pullDeMorgan(Component vertex, Component.Label dualLabel) {
        new PullDeMorganContext(vertex, dualLabel).pull(0);
        markPulled(vertex);
    }

    private final class PullDeMorganContext {
        final Component origin;
        final Component.Label dualLabel;
        final List<Graph.EqClass> inputs;
        final List<List<Component>> candidates;
        final List<Vertex> chosen = new LinkedList<>();

        PullDeMorganContext(Component origin, Component.Label dualLabel) {
            this.origin = origin;
            this.inputs = graph.inputsOf(origin);
            this.dualLabel = dualLabel;
            this.candidates = inputs.stream()
                    .map(cls -> cls.filter(Component.class, hasLabel(Component.Label.NOT)))
                    .toList();
        }

        private void pull(int index) {
            if (index == candidates.size()) {
                var dual = graph.factory().component(dualLabel, origin.outWidth());
                for (var input : chosen) {
                    graph.addEdge(graph.inputsOf(input).getFirst(), dual);
                }
                graph.manager().union(origin, dual);
                markPushed(dual);
            } else {
                for (var candidate : candidates.get(index)) {
                    chosen.add(candidate);
                    pull(index + 1);
                    chosen.removeLast();
                }
            }
        }
    }


    private boolean isPullable(Component vertex) {
        return graph.inputsOf(vertex).stream()
                .allMatch(ec -> ec.contains(Component.class, hasLabel(Component.Label.NOT)));
    }
}
