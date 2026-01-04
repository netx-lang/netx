package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Wire;

import java.util.Optional;

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
        if (!(inputs.get(1) instanceof Component compute) ||
                compute.label() == Component.Label.REGISTER || hasPushed(compute)) {
            return false;
        }

        var clock = (Wire) inputs.getFirst();
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
        return true;
    }

    @Override
    boolean pull(Component vertex) {
        if (vertex.label() == Component.Label.REGISTER || hasPulled(vertex) || !isPullable(vertex)) {
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
            graph.addEdge(graph.inputsOf(input).get(1), dual);
        }
        graph.union(pulled, vertex);
        markPulled(vertex);
        markPushed(dual);
        return true;
    }

    private Optional<Wire> resolveClock(Component vertex) {
        Wire result = null;
        for (var input : graph.inputsOf(vertex)) {
            var clock = (Wire) graph.inputsOf(input).getFirst();
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
        return graph.inputsOf(vertex).stream().allMatch(input ->
                input instanceof  Component component &&
                component.label() == Component.Label.REGISTER);
    }
}
