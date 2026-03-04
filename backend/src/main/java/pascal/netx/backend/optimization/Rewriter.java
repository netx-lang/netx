package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;
import pascal.netx.backend.optimization.model.Vertices;
import pascal.netx.backend.optimization.rule.*;

import java.util.*;
import java.util.stream.Collectors;

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
                new AbsorptionRule(graph),
                new DoubleNegationRule(graph),
                new DistributivityRule(graph),
                new ConstantPropRule(graph)
//                new RetimingRule(graph)
        );
    }

    private final Map<Vertex, BitSet> causality = new HashMap<>();

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

            if (!changed) {
                changed = commonSubExpressionElimination();
            }
        } while (changed);

        var shrink = new ShrinkAssocRule(graph);
        do {
            changed = false;
            for (var vertex : graph.components()) {
                if (shrink.apply(vertex)) {
                    changed = true;
                }
            }
        } while (changed);

        graph.vertices().forEach(this::computeCausalityPreds);
        graph.manager().canonicalize(causality);
    }

    boolean commonSubExpressionElimination() {
        boolean changed = false;
        var map = graph.vertices().stream()
                .collect(Collectors.groupingBy(this::hash));
        for (var group : map.values()) {
            if (group.size() < 2) continue;
            for (int i = 0; i < group.size(); i++) {
                var vi = group.get(i);
                List<Vertex> toMerge = new ArrayList<>();
                for (int j = i + 1; j < group.size(); j++) {
                    var vj = group.get(j);
                    if (vi.name().equals(vj.name()) &&
                            graph.inputsOf(vi).equals(graph.inputsOf(vj))) {
                        toMerge.add(vj);
                    }
                }
                toMerge.forEach(vj -> graph.manager().union(vi, vj));
                toMerge.clear();
            }
        }
        return changed;
    }

    private int hash(Vertex vertex) {
        return Objects.hash(
                graph.inputsOf(vertex).stream()
                        .map(Graph.EqClass::hashCode).toList(),
                vertex.name());
    }

    private void computeCausalityPreds(Vertex v) {
        if (causality.containsKey(v)) {
            return;
        }

        if (!Vertices.isCombinational(v)) {
            causality.put(v, new BitSet());
            return;
        }

        var bitSet = new BitSet();
        causality.put(v, bitSet);
        for (var cls : graph.inputsOf(v)) {
            for (var pred : cls) {
                computeCausalityPreds(pred);
                bitSet.or(causality.get(pred));
                if (Vertices.isCombinational(pred)) {
                    bitSet.set(pred.index());
                }
            }
        }
    }
}
