package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;
import pascal.netx.backend.optimization.model.Vertices;

import javax.annotation.Nonnull;
import java.util.*;

public final class Measurer {
    private final Set<Vertex> vertices;
    private final Graph graph;
    private final Weights weights;

    public Measurer(Graph graph) {
        this(graph, Weights.defaults(graph));
    }

    public Measurer(Graph graph, Weights weights) {
        this.graph = graph;
        this.weights = weights;
        this.vertices = new HashSet<>(graph.vertices());
    }

    public Metrics evaluate() {
        return new Metrics(computeArea(), computeDelay());
    }

    public record Metrics(double area, double delay) {
        @Override
        public @Nonnull String toString() {
            return String.format("Area: %.3f, Delay: %.3f", area, delay);
        }
    }

    private double computeArea() {
        return vertices.stream()
                .map(weights::area)
                .reduce(Double::sum).orElseThrow();
    }

    private double computeDelay() {
        Map<Vertex, Double> dist = new HashMap<>();
        Deque<Vertex> queue = new ArrayDeque<>();
        Set<Vertex> inputs = new HashSet<>(graph.globalIn());
        var visited = new HashSet<Vertex>();
        for (var v : vertices) {
            if (!Vertices.isCombinational(v) || inputs.contains(v)) {
                queue.add(v);
                dist.put(v, 0.0);
            }
        }
        while (!queue.isEmpty()) {
            var v = queue.removeFirst();
            double base = dist.getOrDefault(v, 0.0);
            for (var inCls : graph.inputsOf(v)) {
                for (var in : inCls) {
                    if (!Vertices.isCombinational(in)) {
                        continue;
                    }

                    if (visited.add(in)) {
                        queue.addLast(in);
                    }
                    double candidate = base + weights.delay(in);
                    dist.put(in, Math.max(dist.getOrDefault(in, 0.0), candidate));
                }
            }
        }
        return dist.values().stream()
                .max(Double::compareTo).orElse(0.0);
    }


    public static final class Weights {
        private final Graph graph;
        private final Map<Component.Label, Double> area;
        private final Map<Component.Label, Double> delay;

        public Weights(Graph graph,
                       Map<Component.Label, Double> area,
                       Map<Component.Label, Double> delay) {
            this.graph = graph;
            this.area = area;
            this.delay = delay;
        }

        private static double log2(int n) {
            return Math.log(n) / Math.log(2);
        }

        public double area(Vertex v) {
            int size = graph.inputsOf(v).size();
            if (v instanceof Component c) {
                return area.getOrDefault(c.label(), 1.0) * Math.max(1, log2(size));
            }
            return 0.0;
        }

        public double delay(Vertex v) {
            int size = graph.inputsOf(v).size();
            if (v instanceof Component c) {
                return delay.getOrDefault(c.label(), 1.0) * Math.max(1, log2(size));
            }
            return 0.0;
        }

        public static Weights defaults(Graph graph) {
            var areaBase = new EnumMap<Component.Label, Double>(Component.Label.class);
            var delayBase = new EnumMap<Component.Label, Double>(Component.Label.class);
            for (var label : Component.Label.values()) {
                areaBase.put(label, 1.0);
                delayBase.put(label, 1.0);
            }

            delayBase.put(Component.Label.TO, 0.0);
            delayBase.put(Component.Label.CAST, 0.0);
            delayBase.put(Component.Label.UCAST, 0.0);
            delayBase.put(Component.Label.CONCAT, 0.0);
            delayBase.put(Component.Label.REGISTER, 0.0);
            delayBase.put(Component.Label.ASYNC_REGISTER, 0.0);
            addCommonDefault(delayBase);

            areaBase.put(Component.Label.TO, 0.0);
            areaBase.put(Component.Label.CAST, 0.5);
            areaBase.put(Component.Label.UCAST, 0.5);
            areaBase.put(Component.Label.CONCAT, 0.5);
            areaBase.put(Component.Label.REGISTER, 1.0);
            areaBase.put(Component.Label.ASYNC_REGISTER, 1.0);
            addCommonDefault(areaBase);

            return new Weights(graph, areaBase, delayBase);
        }

        private static void addCommonDefault(EnumMap<Component.Label, Double> map) {
            map.put(Component.Label.NOT, 0.5);
            map.put(Component.Label.ADD, 1.5);
            map.put(Component.Label.SUB, 1.5);
            map.put(Component.Label.SHL, 1.5);
            map.put(Component.Label.ASHR, 1.5);
            map.put(Component.Label.LSHR, 1.5);
            map.put(Component.Label.MUL, 3.0);
            map.put(Component.Label.DIV, 4.0);
            map.put(Component.Label.MOD, 4.0);
        }
    }
}
