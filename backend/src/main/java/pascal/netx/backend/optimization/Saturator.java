package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.*;

import java.util.*;

public final class Saturator {
    private final Graph graph;
    private final double areaWeight;
    private final double delayWeight;

    private final Set<Vertex> outputs;
    private final Map<Vertex, List<Vertex>> classes;

    private final Map<Vertex, Vertex> chosen = new HashMap<>(); // class rep -> chosen vertex

    public Saturator(Graph graph, Set<Vertex> outputs, double areaWeight, double delayWeight) {
        this.graph = graph;
        this.areaWeight = areaWeight;
        this.delayWeight = delayWeight;
        this.outputs = outputs;
        this.classes = graph.equivalenceClasses();
    }

    public void saturate() {
        Deque<Vertex> workList = new ArrayDeque<>();
        Set<Vertex> retain = new HashSet<>();
        Map<Vertex, List<Vertex>> newInputs = new HashMap<>();

        // Seed with outputs, ensuring a chosen representative per equivalence class
        for (var out : outputs) {
            var rep = graph.eqRepOf(out);
            var pick = chooseRepresentative(rep);
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
                var repIn = graph.eqRepOf(in);
                var chosenIn = chooseRepresentative(repIn);
                mappedInputs.add(chosenIn);
                if (!retain.contains(chosenIn)) {
                    workList.add(chosenIn);
                }
            }
            newInputs.put(u, mappedInputs);
        }

        // Rewire inputs of retained vertices to the chosen representatives
        for (var entry : newInputs.entrySet()) {
            var inputs = graph.inputsOf(entry.getKey());
            inputs.clear();
            inputs.addAll(entry.getValue());
        }

        // Prune everything not retained
        for (var v : List.copyOf(graph.vertices())) {
            if (!retain.contains(v)) {
                graph.remove(v);
            }
        }
    }

    private Vertex chooseRepresentative(Vertex clsRep) {
        var rep = graph.eqRepOf(clsRep);
        var existing = chosen.get(rep);
        if (existing != null) {
            return existing;
        }
        var members = classMembers(rep);
        var best = members.stream()
                .min(Comparator
                        .comparing((Vertex v) -> outputs.contains(v) ? 0 : 1)
                        .thenComparingDouble(this::heuristicCost))
                .orElse(rep);
        chosen.put(rep, best);
        return best;
    }

    private List<Vertex> classMembers(Vertex rep) {
        var members = classes.get(rep);
        if (members != null) {
            return members;
        }
        List<Vertex> collected = new ArrayList<>();
        for (var v : graph.vertices()) {
            if (graph.eqRepOf(v) == rep) {
                collected.add(v);
            }
        }
        return collected.isEmpty() ? List.of(rep) : collected;
    }

    private double heuristicCost(Vertex v) {
        int areaCost = (v instanceof Component c && c.label() != Component.Label.REGISTER && c.label() != Component.Label.ASYNC_REGISTER) ? 1 : 0;
        int delayCost = switch (v) {
            case Component c -> switch (c.label()) {
                case REGISTER, ASYNC_REGISTER -> 0;
                case NOT, TO, CAST, UCAST -> 1;
                default -> 2;
            };
            case Wire _, Constant _ -> 0;
            case ReadSelect _, WriteSelect _ -> 100;
        };
        return areaWeight * areaCost + delayWeight * delayCost;
    }
}
