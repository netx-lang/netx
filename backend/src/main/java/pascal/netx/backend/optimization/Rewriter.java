package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Constant;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;

public final class Rewriter {
    private final Graph graph;

    public Rewriter(Graph graph) {
        this.graph = graph;
    }

    public void rewrite() {
        for (var vertex : graph.components()) {
            boolean changed;
            do {
                changed = rewriteIdempotent(vertex);
                changed |= rewriteConstant(vertex);
                changed |= rewriteSingleton(vertex);
                changed |= rewriteShrinkAssoc(vertex);
                changed |= rewriteDoubleNegation(vertex);
            } while (changed);
        }
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
                    if (!graph.equivalent(only, vertex)) {
                        graph.union(vertex, only);
                        yield true;
                    }
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
}
