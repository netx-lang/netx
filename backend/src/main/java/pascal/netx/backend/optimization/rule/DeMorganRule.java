package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class DeMorganRule extends BidiMemRule {
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
                var input = graph.eqClassOf(graph.inputsOf(vertex).getFirst());
                if (input.contains(Component.class, hasLabel(Component.Label.AND))) {
                    var comp = input.get(Component.class, hasLabel(Component.Label.AND));
                    var orComp = graph.factory().component(Component.Label.OR, width);
                    pushDeMorgan(vertex, comp, orComp);
                    return true;
                } else if (input.contains(Component.class, hasLabel(Component.Label.OR))) {
                    var comp = input.get(Component.class, hasLabel(Component.Label.OR));
                    var andComp = graph.factory().component(Component.Label.AND, width);
                    pushDeMorgan(vertex, comp, andComp);
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
            int width = in.outWidth();
            var notIn = graph.factory().component(Component.Label.NOT, width);
            graph.addEdge(in, notIn);
            graph.addEdge(notIn, dual);
        }
        graph.union(vertex, dual);
        markPushed(vertex);
        markPulled(dual);
    }

    @Override
    boolean pull(Component vertex) {
        if (hasPulled(vertex)) {
            return false;
        }

        if (isPullable(vertex)) {
            int width = vertex.outWidth();
            switch (vertex.label()) {
                case AND -> {
                    var nor = graph.factory().component(Component.Label.NOR, width);
                    pullDeMorgan(vertex, nor);
                    return true;
                }
                case OR -> {
                    var nand = graph.factory().component(Component.Label.NAND, width);
                    pullDeMorgan(vertex, nand);
                    return true;
                }
            }
        }
        return false;
    }

    private void pullDeMorgan(Component vertex, Component dual) {
        for (var input : graph.inputsOf(vertex)) {
            var not = graph.eqClassOf(input).get(Component.class, hasLabel(Component.Label.NOT));
            var in = graph.inputsOf(not).getFirst();
            graph.addEdge(in, dual);
        }
        graph.union(vertex, dual);
        markPulled(vertex);
        markPushed(dual);
    }


    private boolean isPullable(Component vertex) {
        return graph.inputsOf(vertex).stream()
                .map(graph::eqClassOf)
                .allMatch(ec -> ec.contains(Component.class, hasLabel(Component.Label.NOT)));
    }
}
