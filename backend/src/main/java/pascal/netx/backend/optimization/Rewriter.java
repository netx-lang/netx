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

    private final HashSet<Vertex> regPushed = new HashSet<>();
    private final HashSet<Vertex> regPulled = new HashSet<>();
    private final HashSet<Vertex> doubleNegWritten = new HashSet<>();
    private final HashSet<Vertex> deMorganWritten = new HashSet<>();

    public Rewriter(Graph graph, Supplier<Integer> indexer) {
        this.graph = graph;
        this.indexer = indexer;
    }

    public void rewrite() {
        onePassRewrite();

        List<Predicate<Component>> rules = List.of(
                this::rewriteIdempotent,
                this::rewriteConstant,
                this::rewriteSingleton,
                this::rewriteShrinkAssoc,
                this::rewriteDeMorgan,
                this::rewriteDoubleNegation,
                this::rewritePullRegister,
                this::rewritePushRegister
        );

        boolean changed;
        do {
            changed = false;
            for (var vertex : graph.components()) {
                for (var rule : rules) {
                    if (rule.test(vertex)) {
                        changed = true;
                        break;
                    }
                }
                if (changed) {
                    break;
                }
            }
        } while (changed);
    }

    private void onePassRewrite() {
        for (var vertex : graph.components()) {
            onePassRewriteIdentity(vertex);
        }
        for (var vertex : graph.wires()) {
            onPassRewriteSelection(vertex);
        }
    }

    private void onePassRewriteIdentity(Component vertex) {
        if (vertex.label() == Component.Label.TO) {
            var input = graph.inputsOf(vertex).getFirst();
            var output = graph.outputsOf(vertex).getFirst();
            graph.replaceAndRemove(output, input);
            graph.replaceAndRemove(vertex, input);
        }
    }

    private void onPassRewriteSelection(Wire vertex) {
        var inputs = graph.inputsOf(vertex).stream()
                .filter(Select.class::isInstance).map(Select.class::cast).toList();
        var outputs = graph.outputsOf(vertex).stream()
                .filter(Select.class::isInstance).map(Select.class::cast).toList();

        for (var selOut : outputs) {
            for (var selIn : inputs) {
                if (selIn.msb() == selOut.msb() && selIn.lsb() == selOut.lsb()) {
                    graph.replaceAndRemove(selOut, selIn);
                }
            }
        }
    }

    private boolean rewriteIdempotent(Component vertex) {
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

    private boolean rewriteSingleton(Component vertex) {
        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER, CAST, NOT, TO -> false;
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

    private boolean rewriteDoubleNegation(Component vertex) {
        if (vertex.label() == Component.Label.NOT && !doubleNegWritten.contains(vertex)) {
            var neg = graph.inputsOf(vertex).getFirst();
            if (neg instanceof Component comp && comp.label() == Component.Label.NOT) {
                var only = graph.inputsOf(neg).getFirst();
                graph.union(vertex, only);
                doubleNegWritten.add(vertex);
                return true;
            }
        }
        return false;
    }

    private boolean rewriteDeMorgan(Component vertex) {
        if (deMorganWritten.contains(vertex)) {
            return false;
        }

        switch (vertex.label()) {
            case NOT -> {
                var input = graph.inputsOf(vertex).getFirst();
                if (input instanceof Component comp) {
                    switch (comp.label()) {
                        case AND -> {
                            var orComp = new Component(indexer.get(), Component.Label.OR);
                            applyDeMorgan(vertex, comp, orComp);
                            deMorganWritten.add(vertex);
                            return true;
                        }
                        case OR -> {
                            var andComp = new Component(indexer.get(), Component.Label.AND);
                            applyDeMorgan(vertex, comp, andComp);
                            deMorganWritten.add(vertex);
                            return true;
                        }
                    }
                }
            }
            case NOR -> {
                var andComp = new Component(indexer.get(), Component.Label.AND);
                applyDeMorgan(vertex, vertex, andComp);
                deMorganWritten.add(vertex);
                return true;
            }
            case NAND ->  {
                var orComp = new Component(indexer.get(), Component.Label.OR);
                applyDeMorgan(vertex, vertex, orComp);
                deMorganWritten.add(vertex);
                return true;
            }
        }
        return false;
    }

    private void applyDeMorgan(Component vertex, Component origin, Component dual) {
        graph.add(dual);
        for (var in : graph.inputsOf(origin)) {
            var notIn = new Component(indexer.get(), Component.Label.NOT);
            graph.add(notIn);
            graph.addEdge(in, notIn);
            graph.addEdge(notIn, dual);
        }
        graph.union(vertex, dual);
    }

    private boolean rewriteConstant(Component vertex) {
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

    private boolean rewriteShrinkAssoc(Component vertex) {
        switch (vertex.label()) {
            case AND, OR, XOR, ADD, MUL -> {
                boolean changed = false;
                List<Vertex> flattened = new LinkedList<>();
                List<Vertex> toRemove = new LinkedList<>();
                for (var input : graph.inputsOf(vertex)) {
                    if (input instanceof Component comp
                            && comp.label() == vertex.label()
                            && graph.outputsOf(comp).size() == 1) {
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

    private boolean rewritePullRegister(Component vertex) {
        if (isRegister(vertex)) {
            return false;
        }

        if (!regPulled.contains(vertex) && pullable(vertex)) {
            var clock = pullClock(vertex);
            if (clock.isPresent()) {
                var pulled = new Component(indexer.get(), Component.Label.REGISTER);
                var compute = new Component(indexer.get(), vertex.label());
                graph.add(pulled);
                graph.add(compute);
                graph.addEdge(clock.get(), pulled);
                graph.addEdge(compute, pulled);
                for (var input : graph.inputsOf(vertex)) {
                    switch (input) {
                        case Constant constant -> graph.addEdge(constant, compute);
                        case Component register -> graph.addEdge(graph.inputsOf(register).get(1), compute);
                        default -> throw new ShallNotReachHereError();
                    }
                }
                graph.union(pulled, vertex);
                regPulled.add(vertex);
                regPushed.add(compute);
                return true;
            }
        }
        return false;
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

    private boolean rewritePushRegister(Component vertex) {
        if (vertex.label() != Component.Label.REGISTER) {
            return false;
        }

        var inputs = graph.inputsOf(vertex);
        var clock = (Wire) inputs.get(0);
        if (inputs.get(1) instanceof Component component) {
            if (isRegister(component) || regPushed.contains(component)) {
                return false;
            }

            var compute = new Component(indexer.get(), component.label());
            graph.add(compute);
            graph.union(compute, vertex);
            for (var input : graph.inputsOf(component)) {
                var pushed = new Component(indexer.get(), Component.Label.REGISTER);
                graph.add(pushed);
                graph.addEdge(pushed, compute);
                graph.addEdge(clock, pushed);
                graph.addEdge(input, pushed);
            }
            regPushed.add(component);
            regPulled.add(compute);
            return true;
        }
        return false;
    }

    private boolean isRegister(Vertex vertex) {
        return vertex instanceof Component comp &&
                (comp.label() == Component.Label.REGISTER || comp.label() == Component.Label.ASYNC_REGISTER);
    }
}
