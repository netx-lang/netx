package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pascal.netx.backend.optimization.model.Vertices.hasLabel;

public final class RetimingRule extends BidiRule {
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

        if (inputs.get(1).contains(Component.class, hasLabel(Component.Label.REGISTER))) {
            return false;
        }

        boolean result = false;
        for (var eqIn : inputs.get(1)) {
            if (!(eqIn instanceof Component compute) || hasPushed(compute)) {
                continue;
            }

            int width = compute.outWidth();
            var dual = graph.factory().component(compute.label(), width);
            graph.manager().union(dual, vertex);
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
        return new PullRetimingContext(vertex).pull();
    }

    private final class PullRetimingContext {
        final Component origin;
        final List<Graph.EqClass> inputs;
        final List<List<Component>> candidates;
        final List<Vertex> chosen = new LinkedList<>();
        boolean changed = false;

        PullRetimingContext(Component origin) {
            this.origin = origin;
            this.inputs = graph.inputsOf(origin);
            this.candidates = inputs.stream()
                    .map(cls -> cls.filter(Component.class, hasLabel(Component.Label.REGISTER)))
                    .toList();
        }

        boolean pull() {
            pull(0);
            if (changed) {
                markPulled(origin);
            }
            return changed;
        }

        void pull(int index) {
            if (index == candidates.size()) {
                var clock = resolveClock(chosen);
                if (clock.isEmpty()) {
                    return;
                }

                int width = origin.outWidth();
                var pulled = graph.factory().component(Component.Label.REGISTER, width);
                var dual = graph.factory().component(origin.label(), width);
                graph.addEdge(clock.get(), pulled);
                graph.addEdge(dual, pulled);
                for (var input : chosen) {
                    graph.addEdge(graph.inputsOf(input).get(1), dual);
                }
                graph.manager().union(pulled, origin);
                markPulled(origin);
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

    private Optional<Graph.EqClass> resolveClock(List<Vertex> registers) {
        Graph.EqClass result = null;
        for (var input : registers) {
            var clock = graph.inputsOf(input).getFirst();
            if (result != null && !result.equals(clock)) {
                return Optional.empty();
            } else {
                result = clock;
            }
        }
        assert result != null;
        return Optional.of(result);
    }

    private boolean isPullable(Component vertex) {
        return !graph.inputsOf(vertex).isEmpty()
                && graph.inputsOf(vertex).stream().allMatch(
                        input -> input.contains(Component.class, hasLabel(Component.Label.REGISTER)));
    }
}
