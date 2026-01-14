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
                .min(Comparator.comparing(this::preferInput).thenComparingDouble(this::scoreOf))
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

    private record Cost(double area, double delay) {
        double score(double areaWeight, double delayWeight) {
            return areaWeight * area + delayWeight * delay;
        }
    }

    private final class CostCalculator {
        private final Map<Vertex, Double> area = new HashMap<>();
        private final Map<Vertex, Double> delay = new HashMap<>();

        private static final int MEM_THRESHOLD = 10;
        private final Map<Vertex, Integer> areaUpdCount = new HashMap<>();

        private final Set<Graph.EqClass> visiting = new HashSet<>();
        private final Map<Vertex, Integer> rank = new HashMap<>();

        Map<Vertex, Cost> computeAll() {
            var result = new HashMap<Vertex, Cost>();
            graph.globalOut().stream()
                    .map(graph::eqClassOf)
                    .flatMap(Graph.EqClass::stream).distinct()
                    .forEach(this::computeArea);
            computeRank();
            graph.vertices().stream().filter(rank::containsKey).forEach(this::computeDelay);
            for (var key : area.keySet()) {
                result.put(key, new Cost(area.get(key), delay.get(key)));
            }
            return result;
        }

        private double computeArea(Vertex vertex) {
            if (areaUpdCount.computeIfAbsent(vertex, _ -> 0) == MEM_THRESHOLD) {
                return area.get(vertex);
            }
            var curr = graph.eqClassOf(vertex);
            visiting.add(curr);

            double result = weights.area(vertex);
            for (var cls : graph.inputsOf(vertex)) {
                if (!visiting.contains(cls)) {
                    double best = Double.MAX_VALUE;
                    for (var pred : cls) {
                        best = Math.min(best, computeArea(pred));
                    }
                    result += best;
                }
            }

            if (area.containsKey(vertex)) {
                if (result == area.get(vertex)) {
                    areaUpdCount.merge(vertex, 1, Integer::sum);
                } else {
                    area.merge(vertex, result, Double::min);
                }
            } else {
                area.put(vertex, result);
            }

            visiting.remove(curr);
            return area.get(vertex);
        }

        private void computeRank() {
            Queue<Vertex> queue = new LinkedList<>();
            graph.globalOut().stream()
                    .map(graph::eqClassOf)
                    .flatMap(Graph.EqClass::stream).distinct()
                    .peek(queue::offer)
                    .forEach(v -> rank.put(v, 0));

            while (!queue.isEmpty()) {
                var vertex = queue.poll();
                for (var cls : graph.inputsOf(vertex)) {
                    for (var pred : cls) {
                        if (!rank.containsKey(pred)) {
                            rank.put(pred, rank.get(vertex) + 1);
                            queue.add(pred);
                        }
                    }
                }
            }
        }

        private double computeDelay(Vertex vertex) {
            if (delay.containsKey(vertex)) {
                return delay.get(vertex);
            }

            double result = 0d;
            for (var cls : graph.inputsOf(vertex)) {
                double best = Double.MAX_VALUE;
                for (var pred : cls) {
                    if (rank.get(pred) > rank.get(vertex)) {
                        best = Math.min(best, computeDelay(pred));
                    }
                }
                if (best != Double.MAX_VALUE) {
                    result = Math.max(result, best);
                }
            }
            result += weights.delay(vertex);
            delay.put(vertex, result);
            return result;
        }
    }
}
