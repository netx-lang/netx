package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.*;
import java.util.function.Function;

public final class Saturator {
    private final Graph graph;

    private final double areaWeight;
    private final double delayWeight;
    private final Measurer.Weights weights = Measurer.Weights.defaults();

    private final Set<Vertex> inputs;
    private final Map<Vertex, List<Vertex>> classes;
    private final Map<Vertex, Vertex> chosen = new HashMap<>(); // class rep -> chosen vertex

    private final Map<Vertex, Cost> costs = new HashMap<>();

    public Saturator(Graph graph, double areaWeight, double delayWeight) {
        this.graph = graph;
        this.areaWeight = areaWeight;
        this.delayWeight = delayWeight;

        this.classes = graph.equivalenceClasses();
        this.inputs = new HashSet<>(graph.inputs());

        costs.putAll(new SubtreeCostCalculator().computeAll());
    }

    public void saturate() {
        Deque<Vertex> workList = new ArrayDeque<>();
        Set<Vertex> retain = new HashSet<>();
        Map<Vertex, List<Vertex>> newInputs = new HashMap<>();

        // Seed with outputs, ensuring a chosen representative per equivalence class
        for (var out : graph.outputs()) {
            var pick = chooseRepresentative(out);
            workList.add(pick);
        }

        // Traverse from outputs to inputs selecting representatives per class
        while (!workList.isEmpty()) {
            var u = workList.pop();
            if (!retain.add(u)) {
                continue;
            }
            List<Vertex> mappedInputs = new ArrayList<>();
            for (var in : graph.inputsOf(u)) {
                var chosenIn = chooseRepresentative(in);
                mappedInputs.add(chosenIn);
                if (!retain.contains(chosenIn)) {
                    workList.add(chosenIn);
                }
            }
            newInputs.put(u, mappedInputs);
        }

        // Rewire inputs of retained vertices to the chosen representatives
        for (var entry : newInputs.entrySet()) {
            var inputs = graph.mutInputsOf(entry.getKey());
            inputs.clear();
            inputs.addAll(entry.getValue());
        }

        // Prune everything not retained
        for (var v : List.copyOf(graph.vertices())) {
            if (!retain.contains(v)) {
                graph.mutVertices().remove(v);
            }
        }
    }

    private Vertex chooseRepresentative(Vertex clsRep) {
        var rep = graph.eqRepOf(clsRep);
        var existing = chosen.get(rep);
        if (existing != null) {
            return existing;
        }
        var members = graph.eqClassOf(rep);
        var best = members.stream()
                .min(Comparator
                        .comparing(this::preferInput)
                        .thenComparing(this::preferWriteSelect)
                        .thenComparingDouble(this::scoreOf))
                .orElseThrow(ShallNotReachHereError::new);
        chosen.put(rep, best);
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
                if (Vertices.isCombinational(in)) { // break cycles at registers
                    var cost = minCostInEqClass(in, this::computeCombinational);
                    childDelay = Math.max(childDelay, cost.delay());
                    area += cost.area();
                }
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
            for (var in : graph.inputsOf(v)) {
                var cost = combCost.getOrDefault(in, baseCost(in));
                area += cost.area();
                childDelay = Math.max(childDelay, cost.delay());
            }
            delay += childDelay;
            finalCost.put(v, new Cost(area, delay));
        }

        private Cost minCostInEqClass(Vertex v, Function<Vertex, Cost> evaluator) {
            var rep = graph.eqRepOf(v);
            List<Vertex> candidates = classes.getOrDefault(rep, List.of(v));
            Cost best = baseCost(v);
            double bestScore = Double.POSITIVE_INFINITY;
            for (var candidate : candidates) {
                var c = evaluator.apply(candidate);
                double score = c.score(areaWeight, delayWeight);
                if (score < bestScore) {
                    best = c;
                    bestScore = score;
                }
            }
            return best;
        }

        private Cost baseCost(Vertex v) {
            return new Cost(weights.area(v), weights.delay(v));
        }
    }
}
