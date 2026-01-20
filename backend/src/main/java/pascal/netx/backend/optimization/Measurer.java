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
        return new DelayComputer().compute();
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


        public static Weights defaults(Graph graph) {
            var area = new EnumMap<Component.Label, Double>(Component.Label.class);
            var delay = new EnumMap<Component.Label, Double>(Component.Label.class);
            for (var label : Component.Label.values()) {
                area.put(label, 1.0);
                delay.put(label, 1.0);
            }

            area.put(Component.Label.TO, 0.0);
            area.put(Component.Label.CAST, 0.5);
            area.put(Component.Label.UCAST, 0.5);
            area.put(Component.Label.SELECT, 0.0);
            area.put(Component.Label.CONCAT, 0.5);
            delay.put(Component.Label.TO, 0.0);
            delay.put(Component.Label.CAST, 0.0);
            delay.put(Component.Label.UCAST, 0.0);
            delay.put(Component.Label.SELECT, 0.0);
            delay.put(Component.Label.CONCAT, 0.0);

            area.put(Component.Label.REGISTER, 1.0);
            area.put(Component.Label.ASYNC_REGISTER, 1.0);
            delay.put(Component.Label.REGISTER, 0.0);
            delay.put(Component.Label.ASYNC_REGISTER, 0.0);

            area.put(Component.Label.NOT, 0.5);
            delay.put(Component.Label.NOT, 0.5);

            area.put(Component.Label.SHL, 6.0);
            area.put(Component.Label.ASHR, 6.0);
            area.put(Component.Label.LSHR, 6.0);
            delay.put(Component.Label.SHL, 2.0);
            delay.put(Component.Label.ASHR, 2.0);
            delay.put(Component.Label.LSHR, 2.0);

            area.put(Component.Label.ADD, 4.0);
            area.put(Component.Label.SUB, 4.0);
            delay.put(Component.Label.ADD, 4.0);
            delay.put(Component.Label.SUB, 4.0);

            area.put(Component.Label.MUL, 40.0);
            delay.put(Component.Label.MUL, 15.0);

            area.put(Component.Label.DIV, 60.0);
            area.put(Component.Label.MOD, 60.0);
            delay.put(Component.Label.DIV, 25.0);
            delay.put(Component.Label.MOD, 25.0);

            return new Weights(graph, area, delay);
        }

        public double area(Vertex v) {
            if (v instanceof Component c) {
                int fanin = graph.inputsOf(v).size();
                return area.getOrDefault(c.label(), 0.0) * Math.max(1, fanin - 3);
            }
            return 0.0;
        }

        int multiFaninDelayCoefficient(int size) {
            return Integer.toBinaryString(Math.max(0, size - 3)).length();
        }

        public double delay(Vertex v) {
            if (v instanceof Component c) {
                int fanin = graph.inputsOf(v).size();
                return delay.getOrDefault(c.label(), 0.0) * multiFaninDelayCoefficient(fanin);
            }
            return 0.0;
        }

    }

    private class DelayComputer {
        Map<Vertex, Double> dist = new HashMap<>();

        DelayComputer() {
            Set<Vertex> inputs = new HashSet<>(graph.globalIn());
            for (var vertex : vertices) {
                if (!Vertices.isCombinational(vertex) || inputs.contains(vertex)) {
                    dist.put(vertex, 0.0);
                }
            }
        }

        double compute() {
            return vertices.stream().map(this::compute).reduce(0.0, Math::max);
        }

        double compute(Vertex v) {
            if (dist.containsKey(v)) {
                return dist.get(v);
            }
            double inDelay = graph.inputsOf(v).stream()
                    .flatMap(Graph.EqClass::stream)
                    .map(this::compute)
                    .reduce(0.0, Math::max);
            dist.put(v, inDelay + weights.delay(v));
            return dist.get(v);
        }
    }
}
