package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

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
                var input = graph.inputsOf(vertex).getFirst();
                if (input instanceof Component comp) {
                    switch (comp.label()) {
                        case AND -> {
                            var orComp = graph.factory().component(Component.Label.OR, width);
                            pushDeMorgan(vertex, comp, orComp);
                            return true;
                        }
                        case OR -> {
                            var andComp = graph.factory().component(Component.Label.AND, width);
                            pushDeMorgan(vertex, comp, andComp);
                            return true;
                        }
                    }
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
                    var notComp = graph.factory().component(Component.Label.NOT, width);
                    var orComp = graph.factory().component(Component.Label.OR, width);
                    pullDeMorgan(vertex, notComp, orComp);
                    return true;
                }
                case OR -> {
                    var notComp = graph.factory().component(Component.Label.NOT, width);
                    var andComp = graph.factory().component(Component.Label.AND, width);
                    pullDeMorgan(vertex, notComp, andComp);
                    return true;
                }
            }
        }
        return false;
    }

    private void pullDeMorgan(Component vertex, Component notComp, Component dual) {
        for (var input : graph.inputsOf(vertex)) {
            var in = graph.inputsOf(input).getFirst();
            graph.addEdge(in, dual);
        }
        graph.addEdge(dual, notComp);
        graph.union(vertex, notComp);
        markPulled(vertex);
        markPushed(notComp);
    }


    private boolean isPullable(Component vertex) {
        return graph.inputsOf(vertex).stream().allMatch(input ->
                input instanceof Component component &&
                component.label() == Component.Label.NOT);
    }
}
