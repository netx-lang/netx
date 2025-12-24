package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class Rewriter {
    private final Graph graph;
    private final Supplier<Integer> indexer;

    private final HashSet<Vertex> regPulled = new HashSet<>();

    public Rewriter(Graph graph, Supplier<Integer> indexer) {
        this.graph = graph;
        this.indexer = indexer;
    }

    public void rewrite() {
        boolean changed = true;
        do {
            changed = false;
            for (var vertex : graph.components()) {
                changed |= rewriteIdempotent(vertex);
                changed |= rewriteConstant(vertex);
                changed |= rewriteSingleton(vertex);
                changed |= rewriteShrinkAssoc(vertex);
                changed |= rewriteDoubleNegation(vertex);
                changed |= rewritePullRegister(vertex);
            }
        } while (changed);
    }

    public boolean rewriteIdempotent(Component vertex) {
        return switch (vertex.label()) {
            case AND, OR, XOR, NAND, NOR, XNOR -> {
                var inputs = graph.inputsOf(vertex);
                var seen = inputs.stream().distinct().toList();
                if (seen.size() < inputs.size()) {
                    inputs.clear();
                    inputs.addAll(seen);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public boolean rewriteSingleton(Component vertex) {
        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER -> false;
            default -> {
                var inputs = graph.inputsOf(vertex);
                if (inputs.size() == 1) {
                    var only = inputs.getFirst();
                    graph.replaceAndRemove(vertex, only);
                    yield true;
                }
                yield false;
            }
        };
    }

    public boolean rewriteDoubleNegation(Component vertex) {
        if (vertex.label() == Component.Label.NOT) {
            var neg = graph.inputsOf(vertex).getFirst();
            if (neg instanceof Component comp && comp.label() == Component.Label.NOT) {
                var only = graph.inputsOf(neg).getFirst();
                graph.union(vertex, only);
            }
        }
        return false;
    }

    public boolean rewriteConstant(Component vertex) {
        return switch (vertex.label()) {
            case AND -> rewriteConstant(vertex, Constant::isMersenne);
            case OR -> rewriteConstant(vertex, Constant::isZero);
            default -> false;
        };
    }

    private boolean rewriteConstant(Vertex vertex, Predicate<Constant> predicate) {
        return graph.inputsOf(vertex).removeIf(v -> {
            if (v instanceof Constant constant) {
                return predicate.test(constant);
            }
            return false;
        });
    }

    public boolean rewriteShrinkAssoc(Component vertex) {
        switch (vertex.label()) {
            case AND, OR, XOR, ADD, MUL -> {
                boolean changed = false;
                List<Vertex> flattened = new LinkedList<>();
                List<Vertex> toRemove = new LinkedList<>();
                for (var input : graph.inputsOf(vertex)) {
                    if (input instanceof Component comp && comp.label() == vertex.label()) {
                        rewriteShrinkAssoc(comp);
                        flattened.addAll(graph.inputsOf(comp));
                        toRemove.add(comp);
                        changed = true;
                    } else {
                        flattened.add(input);
                    }
                }
                if (changed) {
                    graph.inputsOf(vertex).clear();
                    graph.inputsOf(vertex).addAll(flattened);
                    toRemove.forEach(graph::remove);
                    return true;
                }
            }
        }
        return false;
    }

    public boolean rewritePullRegister(Component vertex) {
        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER -> false;
            default -> {
                if (!regPulled.contains(vertex) && pullable(vertex)) {
                    var clock = pullClock(vertex);
                    if (clock.isPresent()) {
                        var pulled = new Component(indexer.get(), Component.Label.REGISTER);
                        var compute = new Component(indexer.get(), vertex.label());
                        graph.add(pulled);
                        graph.add(compute);
                        graph.addEdge(pulled, clock.get());
                        graph.addEdge(pulled, compute);
                        for (var input : graph.inputsOf(vertex)) {
                            switch (input) {
                                case Constant constant -> graph.addEdge(compute, constant);
                                case Component register -> graph.addEdge(compute, graph.inputsOf(register).get(1));
                                default -> throw new ShallNotReachHereError();
                            }
                        }
                        graph.union(pulled, vertex);
                        regPulled.add(vertex);
                        yield true;
                    }
                }
                yield false;
            }
        };
    }

    private boolean pullable(Component vertex) {
        var inputs = graph.inputsOf(vertex);
        boolean hasReg = inputs.stream().anyMatch(Component.class::isInstance);
        return hasReg && inputs.stream().allMatch(input ->
                switch (input) {
                    case Component comp -> comp.label() == Component.Label.REGISTER;
                    case Constant _ -> true;
                    default -> false;
                }
        );
    }

    private Optional<Wire> pullClock(Component vertex) {
        Wire result = null;
        for (var input : graph.inputsOf(vertex)) {
            if (input instanceof Component comp && comp.label() == Component.Label.REGISTER) {
                var clock = (Wire) graph.inputsOf(comp).getFirst();
                if (result != null && !graph.equivalent(result, clock)) {
                    return Optional.empty();
                } else {
                    result = clock;
                }
            }
        }
        assert result != null;
        return Optional.of(result);
    }

}
