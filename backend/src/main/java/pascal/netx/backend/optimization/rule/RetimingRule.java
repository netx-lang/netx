package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.*;

import java.util.Optional;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class RetimingRule extends BidiMemRule {
    public RetimingRule(Graph graph) {
        super(graph);
    }

    @Override
    boolean push(Component vertex) {
        if (vertex.label() != Component.Label.REGISTER) {
            return false;
        }

        var inputs = graph.inputsOf(vertex);
        var clock = inputs.getFirst();

        boolean result = false;
        for (var eqIn : graph.eqClassOf(inputs.get(1))) {
            if (!(eqIn instanceof Component compute) ||
                    compute.label() == Component.Label.REGISTER || hasPushed(compute)) {
                continue;
            }

            int width = compute.outWidth();
            var dual = graph.factory().component(compute.label(), width);
            graph.union(dual, vertex);
            for (var input : graph.inputsOf(compute)) {
                var pushed = graph.factory().component(Component.Label.REGISTER, width);
                graph.addEdge(pushed, dual);
                graph.addEdge(clock, pushed);
                graph.addEdge(input, pushed);
            }
            markPushed(compute);
            markPulled(dual);
            result = true;
        }

        return result;
    }

    @Override
    boolean pull(Component vertex) {
        if (vertex.label() == Component.Label.REGISTER
                || hasPulled(vertex)
                || !isPullable(vertex)) {
            return false;
        }

        var clock = resolveClock(vertex);
        if (clock.isEmpty()) {
            return false;
        }

        int width = vertex.outWidth();
        var pulled = graph.factory().component(Component.Label.REGISTER, width);
        var dual = graph.factory().component(vertex.label(), width);
        graph.addEdge(clock.get(), pulled);
        graph.addEdge(dual, pulled);
        for (var input : graph.inputsOf(vertex)) {
            var reg = graph.eqClassOf(input).get(Component.class, hasLabel(Component.Label.REGISTER));
            graph.addEdge(graph.inputsOf(reg).get(1), dual);
        }
        graph.union(pulled, vertex);
        markPulled(vertex);
        markPushed(dual);
        return true;
    }

    private Optional<Vertex> resolveClock(Component vertex) {
        Vertex result = null;
        for (var input : graph.inputsOf(vertex)) {
            var reg = graph.eqClassOf(input).get(Component.class, hasLabel(Component.Label.REGISTER));
            var clock = graph.inputsOf(reg).getFirst();
            if (result != null && !graph.equivalent(result, clock)) {
                return Optional.empty();
            } else {
                result = clock;
            }
        }
        assert result != null;
        return Optional.of(result);
    }

    private boolean isPullable(Component vertex) {
        return graph.inputsOf(vertex).stream()
                .map(graph::eqClassOf)
                .allMatch(input -> input.contains(Component.class, hasLabel(Component.Label.REGISTER)));
    }
}
