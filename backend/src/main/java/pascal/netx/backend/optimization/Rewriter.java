package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;
import pascal.netx.backend.optimization.model.Vertices;
import pascal.netx.backend.optimization.rule.*;

import java.util.*;

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
                new ConstantPropRule(graph),
                new DeMorganRule(graph),
                new RetimingRule(graph)
        );
    }

    private final Map<Vertex, Set<Vertex>> causality = new HashMap<>();

    public void rewrite() {
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
            }
        } while (changed);

        graph.vertices().forEach(this::computeCausalityPreds);
        graph.manager().canonicalize(causality);
    }

    private void computeCausalityPreds(Vertex v) {
        if (!Vertices.isCombinational(v)) {
            causality.put(v, Set.of());
            return;
        }

        if (causality.containsKey(v)) {
            return;
        }

        var set = new HashSet<Vertex>();
        causality.put(v, set);
        for (var cls : graph.inputsOf(v)) {
            for (var pred : cls) {
                computeCausalityPreds(pred);
                set.addAll(causality.get(pred));
                set.add(pred);
            }
        }
    }
}
