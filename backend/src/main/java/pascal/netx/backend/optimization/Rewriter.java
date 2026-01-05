package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.backend.optimization.rule.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class Rewriter {
    private final Graph graph;
    private final List<Rule> rules;

    public Rewriter(Graph graph) {
        this.graph = graph;
        this.rules = List.of(
                new IdempotentRule(graph),
                new IdentityRule(graph),
                new SingletonRule(graph),
                new ShrinkAssocRule(graph),
                new AbsorptionRule(graph),
                new DoubleNegationRule(graph),
                new DeMorganRule(graph),
                new RetimingRule(graph)
//                new DistributivityRule(graph)
        );
    }

    public void rewrite() {
        onePassRewrite();
        boolean changed;
        do {
            changed = false;
            for (var rule : rules) {
                for (var vertex : graph.components()) {
                    if (rule.apply(vertex)) {
                        changed = true;
                    }
                }
            }
        } while (changed);
    }

    private void onePassRewrite() {
        onePassRewriteIdentity();
        onePassRewriteConcat();
    }

    private void onePassRewriteIdentity() {
        List<Component> removalOrder = new ArrayList<>();
        Set<Component> visited = new HashSet<>();
        graph.components().stream()
                .filter(component -> component.label() == Component.Label.TO)
                .forEach(to -> resolveOrder(removalOrder, visited, to));

        for (var to : removalOrder) {
            if (!graph.vertices().contains(to)) {
                continue;
            }

            graph.union(graph.inputsOf(to).getFirst(), to);
        }
    }

    private void resolveOrder(List<Component> order, Set<Component> visited, Component current) {
        if (!visited.add(current)) {
            return;
        }
        
        for (var out : graph.inputsOf(current)) {
            if (out instanceof Component next && next.label() == Component.Label.TO) {
                resolveOrder(order, visited, next);
            }
        }
        order.add(current);
    }

    private void onePassRewriteConcat() {
        for (var vertex : graph.components()) {
            if (vertex.label() == Component.Label.CONCAT) {
                Map<Selector, Vertex> inputs = new HashMap<>();
                int index = 0;
                for (var input : graph.inputsOf(vertex).reversed()) {
                    int width = input.outWidth();
                    Selector selector = new Selector(index, index + width - 1);
                    inputs.put(selector, input);
                    index += width;
                }

                for (var output : graph.outputsOf(vertex)) {
                    if (output instanceof ReadSelect select) {
                        if (inputs.containsKey(select.selector())) {
                            graph.union(output, inputs.get(select.selector()));
                        }
                    }
                }
            }
        }
    }
}
