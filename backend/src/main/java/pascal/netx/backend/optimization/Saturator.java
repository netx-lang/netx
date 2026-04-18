package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;
import pascal.netx.util.exception.ShallNotReachHereError;

import java.util.*;

public final class Saturator {
    private final Graph graph;

    private final double areaWeight;
    private final double delayWeight;
    private final Measurer.Weights weights;

    private final Set<Vertex> inputs;
    private final Map<Graph.EqClass, Vertex> chosen = new HashMap<>();

    private final Map<Vertex, Cost> costs = new HashMap<>();

    public Saturator(Graph graph, double areaWeight, double delayWeight) {
        this.graph = graph;
        this.areaWeight = areaWeight;
        this.delayWeight = delayWeight;
        this.weights = Measurer.Weights.defaults(graph);
        this.inputs = new HashSet<>(graph.globalIn());
        costs.putAll(new CostCalculator().computeAll());
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

    private Vertex chooseRepresentative(Graph.EqClass cls) {
        var existing = chosen.get(cls);
        if (existing != null) {
            return existing;
        }

        var best = cls.stream()
                .min(Comparator.comparing(this::scoreOf).thenComparingDouble(this::preferInput).thenComparing(Vertex::index))
                .orElseThrow(ShallNotReachHereError::new);
        chosen.put(cls, best);
        return best;
    }

    private int preferInput(Vertex v) {
        return inputs.contains(v) ? 0 : 1;
    }

    private double scoreOf(Vertex v) {
        return costs.get(v).score(areaWeight, delayWeight);
    }

    private record Cost(double area, double delay) {
        double score(double areaWeight, double delayWeight) {
            return areaWeight * area + delayWeight * delay;
        }
    }

    private final class CostCalculator {
        private static final int RECOMPUTE = 100;
        private final Map<Vertex, Integer> count = new HashMap<>();
        private final Set<Vertex> inStack = new HashSet<>();

        private final Map<Vertex, Cost> result = new HashMap<>();

        Map<Vertex, Cost> computeAll() {
            graph.globalOut().stream()
                    .map(graph::eqClassOf)
                    .flatMap(Graph.EqClass::stream).distinct()
                    .forEach(this::compute);
            return result;
        }

        double score(Cost cost) {
            return cost.score(areaWeight, delayWeight);
        }

        private Cost compute(Vertex v) {
            count.computeIfAbsent(v, _ -> 0);
            count.merge(v, 1, Integer::sum);
            if (count.get(v) > RECOMPUTE) {
                return result.get(v);
            }

            inStack.add(v);

            List<Cost> toMerge = new LinkedList<>();
            for (var cls : graph.inputsOf(v)) {
                double best = Double.POSITIVE_INFINITY;
                Cost bestCost = null;
                for (var pred : cls) {
                    if (!inStack.contains(pred)) {
                        var predCost = compute(pred);
                        var predScore = score(predCost);
                        if (predScore < best) {
                            best = predScore;
                            bestCost = predCost;
                        }
                    }
                }
                if (bestCost != null) {
                    toMerge.add(bestCost);
                }
            }

            double predDelay = toMerge.stream().map(Cost::delay)
                    .max(Comparator.naturalOrder()).orElse(0d);
            double predArea = toMerge.stream().map(Cost::area)
                    .reduce(0d, Double::sum);
            Cost cost = new Cost(
                    weights.area(v) + predArea,
                    weights.delay(v) + predDelay
            );
            inStack.remove(v);
            result.merge(v, cost, (existing, incoming) ->
                    score(incoming) < score(existing) ? incoming : existing);
            return result.get(v);
        }
    }
}
