package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Register;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static pascal.netx.backend.optimization.rule.GraphRule.Marker.RETIMING_PULL;
import static pascal.netx.backend.optimization.rule.GraphRule.Marker.RETIMING_PUSH;

public final class RetimingRule extends BidiRule {
    public RetimingRule(Graph graph) {
        super(graph);
    }

    @Override
    boolean push(Component vertex) {
        if (!(vertex instanceof Register reg)) {
            return false;
        }

        var inputs = graph.inputsOf(vertex);
        var clock = inputs.getFirst();

        if (inputs.get(1).contains(Register.class)) {
            return false;
        }

        boolean result = false;
        for (var eqIn : inputs.get(1)) {
            if (!(eqIn instanceof Component compute) || hasMarker(compute, RETIMING_PUSH)) {
                continue;
            }

            var dual = graph.factory().copy(compute);
            graph.manager().union(dual, vertex);
            for (var input : graph.inputsOf(compute)) {
                var pushed = graph.factory().register(input.width(), reg.isPosEdge());
                migrateMarkers(reg, pushed);
                graph.addEdge(pushed, dual);
                graph.addEdge(clock, pushed);
                graph.addEdge(input, pushed);
                if (dual instanceof Component component
                        && component.label() == Component.Label.NOT) {
                    mark(pushed, RETIMING_PUSH);
                }
            }
            migrateMarkers(compute, dual);
            mark(compute, RETIMING_PUSH);
            mark(dual, RETIMING_PULL);
            result = true;
        }

        return result;
    }

    @Override
    boolean pull(Component vertex) {
        if (graph.eqClassOf(vertex).contains(Register.class)
                || hasMarker(vertex, RETIMING_PULL)
                || !isPullable(vertex)) {
            return false;
        }
        return new PullRetimingContext(vertex).pull();
    }

    private Optional<Boolean> resolveEdge(List<Register> registers) {
        Boolean isPosEdge = null;
        for (var input : registers) {
            if (isPosEdge != null && isPosEdge != input.isPosEdge()) {
                return Optional.empty();
            } else {
                isPosEdge = input.isPosEdge();
            }
        }
        assert isPosEdge != null;
        return Optional.of(isPosEdge);
    }

    private Optional<Graph.EqClass> resolveClock(List<Register> registers) {
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
                input -> input.contains(Register.class));
    }

    private final class PullRetimingContext {
        final Component origin;
        final List<Graph.EqClass> inputs;
        final List<List<Register>> candidates;
        final List<Register> chosen = new LinkedList<>();
        boolean changed = false;

        PullRetimingContext(Component origin) {
            this.origin = origin;
            this.inputs = graph.inputsOf(origin);
            this.candidates = inputs.stream()
                    .map(cls -> cls.filter(Register.class))
                    .toList();
        }

        boolean pull() {
            pull(0);
            if (changed) {
                mark(origin, RETIMING_PULL);
            }
            return changed;
        }

        void pull(int index) {
            if (index == candidates.size()) {
                var clock = resolveClock(chosen);
                var isPosEdge = resolveEdge(chosen);
                if (clock.isEmpty() || isPosEdge.isEmpty()) {
                    return;
                }

                int width = origin.outWidth();
                var pulled = graph.factory().register(width, isPosEdge.get());
                var dual = graph.factory().copy(origin);
                migrateMarkers(origin, dual);
                graph.addEdge(clock.get(), pulled);
                graph.addEdge(dual, pulled);
                for (var input : chosen) {
                    graph.addEdge(graph.inputsOf(input).get(1), dual);
                    migrateMarkers(input, pulled);
                }
                graph.manager().union(pulled, origin);
                mark(pulled, RETIMING_PUSH);
            } else {
                for (var candidate : candidates.get(index)) {
                    chosen.add(candidate);
                    pull(index + 1);
                    chosen.removeLast();
                }
            }
        }
    }
}
