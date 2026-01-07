package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.backend.optimization.rule.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Rewriter {
    private final Graph graph;
    private final List<Rule> rules;

    public Rewriter(Graph graph) {
        this.graph = graph;
        this.rules = List.of(
                new IdempotentRule(graph),
                new RedundantConstRule(graph),
                new SingletonRule(graph),
                new ShrinkAssocRule(graph),
                new AbsorptionRule(graph),
                new DoubleNegationRule(graph),
                new DistributivityRule(graph),
                new DeMorganRule(graph),
                new RetimingRule(graph)
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
        onePassRewriteConcat();
    }

    private void onePassRewriteConcat() {
        for (var vertex : graph.components()) {
            if (vertex.label() == Component.Label.CONCAT) {
                Map<Selector, Graph.EqClass> inputs = new HashMap<>();
                int index = 0;
                for (var input : graph.inputsOf(vertex).reversed()) {
                    int width = input.getAny().outWidth();
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
