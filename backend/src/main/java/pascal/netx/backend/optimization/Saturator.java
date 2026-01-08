package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.*;

public final class Saturator {
    private final Graph graph;

    private final double areaWeight;
    private final double delayWeight;
    private final Measurer.Weights weights;

    private final Set<Vertex> inputs;
    private final Map<Graph.EqClass, Vertex> chosen = new HashMap<>(); // class rep -> chosen vertex

    private final Map<Vertex, Cost> costs = new HashMap<>();
    private final Map<Vertex, Set<Vertex>> causality = new HashMap<>(); // vertex -> combinational predecessors

    public Saturator(Graph graph, double areaWeight, double delayWeight) {
        this.graph = graph;
        this.areaWeight = areaWeight;
        this.delayWeight = delayWeight;
        this.weights = Measurer.Weights.defaults(graph);

        this.inputs = new HashSet<>(graph.globalIn());
        costs.putAll(new SubtreeCostCalculator().computeAll());
        for (var v : graph.vertices()) {
            computeCausalityPreds(v);
        }
    }

    public void saturate() {
        Deque<Vertex> workList = new ArrayDeque<>();
        Set<Vertex> retain = new HashSet<>();

        // Seed with outputs, ensuring a chosen representative per equivalence class
        for (var out : graph.globalOut()) {
            var pick = chooseRepresentative(graph.eqClassOf(out));
            workList.add(pick);
        }

        // Traverse from outputs to inputs selecting representatives per class
        while (!workList.isEmpty()) {
            var u = workList.pop();
            if (!retain.add(u)) {
                continue;
            }
            for (var in : graph.inputsOf(u)) {
                var chosenIn = chooseRepresentative(in);
                if (!retain.contains(chosenIn)) {
                    workList.add(chosenIn);
                }
            }
        }

        graph.retain(retain);
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

    private Vertex chooseRepresentative(Graph.EqClass cls) {
        var existing = chosen.get(cls);
        if (existing != null) {
            return existing;
        }

        var best = cls.stream()
                .filter(u -> cls.stream().noneMatch(causality.get(u)::contains))
                .min(Comparator
                        .comparing(this::preferInput)
                        .thenComparing(this::preferWriteSelect)
                        .thenComparingDouble(this::scoreOf))
                .orElseThrow(ShallNotReachHereError::new);
        chosen.put(cls, best);
        return best;
    }

    private int preferInput(Vertex v) {
        return inputs.contains(v) ? 0 : 1;
    }

    private double scoreOf(Vertex v) {
        var cost = costs.get(v);
        if (cost == null) {
            return 0.0;
        }
        return cost.score(areaWeight, delayWeight);
    }

    private int preferWriteSelect(Vertex v) {
        if (v instanceof WriteSelect) return 0;
        if (v instanceof ReadSelect) return 1;
        return 0; // neutral for others
    }

    private record Cost(double area, double delay) {
        double score(double areaWeight, double delayWeight) {
            return areaWeight * area + delayWeight * delay;
        }
    }

    private final class SubtreeCostCalculator {
        private final Map<Vertex, Cost> combCost = new HashMap<>();
        private final Map<Vertex, Cost> finalCost = new HashMap<>();
        private final Set<Vertex> visiting = new HashSet<>();

        Map<Vertex, Cost> computeAll() {
            graph.vertices().forEach(this::computeCombinational);
            graph.vertices().forEach(this::computeSequential);
            return finalCost;
        }

        private Cost computeCombinational(Vertex v) {
            var cached = combCost.get(v);
            if (cached != null) {
                return cached;
            }
            if (!visiting.add(v)) {
                return baseCost(v);
            }

            double area = weights.area(v), delay = weights.delay(v);
            double childDelay = 0.0;
            for (var in : graph.inputsOf(v)) {
                var cost = minCostInEqClass(in);
                childDelay = Math.max(childDelay, cost.delay());
                area += cost.area();
            }
            delay += childDelay;

            visiting.remove(v);
            var result = new Cost(area, delay);
            combCost.put(v, result);
            return result;
        }

        private void computeSequential(Vertex v) {
            double area = weights.area(v), delay = weights.delay(v);
            double childDelay = 0.0;
            for (var cls : graph.inputsOf(v)) {
                var best = Double.POSITIVE_INFINITY;
                var cost = new Cost(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
                for (var in : cls) {
                    var c = combCost.getOrDefault(in, baseCost(in));
                    double score = c.score(areaWeight, delayWeight);
                    if (score < best) {
                        best = score;
                        cost = c;
                    }
                }
                area += cost.area();
                childDelay = Math.max(childDelay, cost.delay());
            }
            delay += childDelay;
            finalCost.put(v, new Cost(area, delay));
        }

        private Cost minCostInEqClass(Graph.EqClass cls) {
            Cost best = new Cost(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY);
            double bestScore = Double.POSITIVE_INFINITY;
            for (var candidate : cls) {
                if (Vertices.isCombinational(candidate)) { // break cycles at registers
                    var c = computeCombinational(candidate);
                    double score = c.score(areaWeight, delayWeight);
                    if (score < bestScore) {
                        best = c;
                        bestScore = score;
                    }
                }
            }
            return best;
        }

        private Cost baseCost(Vertex v) {
            return new Cost(weights.area(v), weights.delay(v));
        }
    }
}
