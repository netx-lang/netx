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
                new IdempotentRule(graph),
                new IdentityRule(graph),
                new SingletonRule(graph),
                new ShrinkAssocRule(graph),
                new DeMorganRule(graph),
                new DoubleNegationRule(graph),
                new RetimingRule(graph)
        );
    }

    public void rewrite() {
        onePassRewrite();
        boolean changed;
        do {
            changed = false;
            for (var vertex : graph.components()) {
                for (var rule : rules) {
                    if (rule.apply(vertex)) {
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
    }

    private void onePassRewriteIdentity(Component vertex) {
        if (vertex.label() == Component.Label.TO) {
            var input = graph.inputsOf(vertex).getFirst();
            graph.replaceAndRemove(vertex, input);
        }
    }
}
