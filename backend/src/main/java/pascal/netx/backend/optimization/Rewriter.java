package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.backend.optimization.rule.*;

import java.util.List;

public final class Rewriter {
    private final Graph graph;
    private final List<Rule> rules;

    public Rewriter(Graph graph) {
        this.graph = graph;
        this.rules = List.of(
                new ConcatShortcutRule(graph),
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


}
