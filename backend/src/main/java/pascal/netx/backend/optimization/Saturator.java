package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;
import pascal.netx.backend.optimization.model.Vertices;
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
        costs.putAll(new CostCalculator().computeAll());
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

        private final Set<Graph.EqClass> visiting = new HashSet<>();


        Map<Vertex, Cost> computeAll() {
            var result = new HashMap<Vertex, Cost>();
            graph.globalOut().stream()
                    .map(graph::eqClassOf).distinct()
                    .flatMap(Graph.EqClass::stream).distinct()
                    .forEach(this::computeArea);
            area.forEach((k, v) -> result.put(k, new Cost(v, 0)));
            return result;
        }

        private double computeArea(Vertex vertex) {
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
                area.merge(vertex, result, Double::min);
            } else {
                area.put(vertex, result);
            }

            visiting.remove(curr);
            return result;
        }

        private void computeDelay(Vertex vertex) {

        }
    }
}
