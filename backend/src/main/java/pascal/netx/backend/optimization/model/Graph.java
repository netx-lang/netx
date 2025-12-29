package pascal.netx.backend.optimization.model;

import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static pascal.netx.backend.NxonParseUtils.quote;

public final class Graph {
    private final List<Vertex> inputs = new ArrayList<>();
    private final List<Vertex> outputs = new ArrayList<>();

    private final Set<Vertex> vertices = new HashSet<>();
    private final Map<Vertex, List<Vertex>> inEdges = new HashMap<>();
    private final Map<Vertex, List<Vertex>> outEdges = new HashMap<>();

    private final UnionFind<Vertex> equivalence = new UnionFind<>();

    private final AtomicInteger indexer = new AtomicInteger();
    private final Map<BitVector, Constant> constantCache = new HashMap<>();

    public void setInputs(List<Vertex> inputs) {
        this.inputs.addAll(inputs);
    }

    public void setOutputs(List<Vertex> outputs) {
        this.outputs.addAll(outputs);
    }

    public List<Vertex> outputs() {
        return outputs;
    }

    public List<Vertex> inputs() {
        return inputs;
    }

    private void add(Vertex vertex) {
        vertices.add(vertex);
        equivalence.add(vertex);
        inEdges.putIfAbsent(vertex, new LinkedList<>());
        outEdges.putIfAbsent(vertex, new LinkedList<>());
    }

    public void remove(Vertex vertex) {
        vertices.remove(vertex);
        inEdges.remove(vertex);
        outEdges.remove(vertex);
        for (var targets : inEdges.values()) {
            targets.remove(vertex);
        }
        for (var targets : outEdges.values()) {
            targets.remove(vertex);
        }
    }

    public void addEdge(Vertex from, Vertex to) {
        outEdges.get(from).add(to);
        inEdges.get(to).add(from);
    }

    public boolean equivalent(Vertex a, Vertex b) {
        return equivalence.find(a) == equivalence.find(b);
    }

    public void union(Vertex a, Vertex b) {
        equivalence.union(a, b);
    }

    private int nextIndex() {
        return indexer.getAndIncrement();
    }

    public Wire newWire(String label, int width) {
        var wire = new Wire(nextIndex(), label, width);
        add(wire);
        return wire;
    }

    public WriteSelect newWriteSelect(String label, Selector selector) {
        var select = new WriteSelect(nextIndex(), label, selector);
        add(select);
        return select;
    }

    public ReadSelect newReadSelect(Selector selector) {
        var select = new ReadSelect(nextIndex(), selector);
        add(select);
        return select;
    }

    public Component newComponent(Component.Label label, int width) {
        var component = new Component(nextIndex(), label, width);
        add(component);
        return component;
    }

    public Component newComponent(String label, int width) {
        var component = new Component(nextIndex(), label, width);
        add(component);
        return component;
    }

    public Constant newConstant(BitVector value) {
        if (constantCache.containsKey(value)) {
            return constantCache.get(value);
        } else {
            var constant = new Constant(nextIndex(), value);
            constantCache.put(value, constant);
            add(constant);
            return constant;
        }
    }

    public void replaceAndRemove(Vertex oldVertex, Vertex newVertex) {
        for (var entry : inEdges.entrySet()) {
            entry.getValue().replaceAll(value -> value.equals(oldVertex) ? newVertex : value);
        }
        for (var entry : outEdges.entrySet()) {
            entry.getValue().replaceAll(value -> value.equals(oldVertex) ? newVertex : value);
        }
        inputs.replaceAll(value -> value.equals(oldVertex) ? newVertex : value);
        outputs.replaceAll(value -> value.equals(oldVertex) ? newVertex : value);
        remove(oldVertex);
    }

    public List<Vertex> inputsOf(Vertex vertex) {
        return inEdges.getOrDefault(vertex, List.of());
    }

    public List<Vertex> outputsOf(Vertex vertex) {
        return outEdges.getOrDefault(vertex, List.of());
    }

    public Collection<Component> components() {
        return vertices.stream().filter(Component.class::isInstance).map(Component.class::cast).toList();
    }

    public Collection<Wire> wires() {
        return vertices.stream().filter(Wire.class::isInstance).map(Wire.class::cast).toList();
    }

    public Collection<Vertex> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    public Map<Vertex, List<Vertex>> equivalenceClasses() {
        Map<Vertex, List<Vertex>> result = new LinkedHashMap<>();
        for (var v : vertices) {
            var rep = equivalence.find(v);
            result.computeIfAbsent(rep, _ -> new ArrayList<>()).add(v);
        }
        return result;
    }

    public void dump(NotepadWriter writer) {
        var ordered = new ArrayList<>(vertices);
        ordered.sort(Comparator.comparingInt(Vertex::index));

        // Group vertices by their union-find representative in a stable order
        Map<Vertex, List<Vertex>> groups = new LinkedHashMap<>();
        for (var v : ordered) {
            var rep = equivalence.find(v);
            groups.computeIfAbsent(rep, _ -> new ArrayList<>()).add(v);
        }

        writer.println("digraph G {");
        writer.incIndent();

        // Emit clusters for equivalence classes with more than one member
        Set<Vertex> emitted = new HashSet<>();
        int clusterIdx = 0;
        for (var entry : groups.entrySet()) {
            var members = entry.getValue();
            // avoid embedding '\n' in format strings: print then newline
            writer.printf("subgraph cluster_%d {", clusterIdx++);
            writer.newLine();
            writer.incIndent();
            writer.println("style=dashed;");
            for (var v : members) {
                var name = "v" + v.index();
                var label = quote(v.name());
                writer.printf("%s [label=%s];", name, label);
                writer.newLine();
                emitted.add(v);
            }
            writer.decIndent();
            writer.println("}");
            writer.newLine();
        }

        // Emit singleton nodes (not part of multi-member equivalence clusters)
        for (var v : ordered) {
            if (emitted.contains(v)) continue;
            var name = "v" + v.index();
            var label = quote(v.name());
            writer.printf("%s [label=%s];", name, label);
            writer.newLine();
        }

        writer.newLine();

        // Emit edges
        for (var v : ordered) {
            var fromName = "v" + v.index();
            var targets = inEdges.getOrDefault(v, List.of());
            for (var t : targets) {
                var toName = "v" + t.index();
                writer.printf("%s -> %s;", toName, fromName);
                writer.newLine();
            }
        }

        writer.decIndent();
        writer.println("}");
    }

    public Vertex eqRepOf(Vertex vertex) {
        return equivalence.find(vertex);
    }
}
