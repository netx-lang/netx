package pascal.netx.backend.optimization.model;

import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.print.NotepadWriter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static pascal.netx.backend.NxonParseUtils.quote;

public final class Graph {
    private final List<Vertex> inputs = new ArrayList<>();
    private final List<Vertex> outputs = new ArrayList<>();

    private final Set<Vertex> vertices = new HashSet<>();
    private final Map<Vertex, List<Vertex>> inEdges = new HashMap<>();
    private final Map<Vertex, List<Vertex>> outEdges = new HashMap<>();

    private final UnionFind<Vertex> unionFind = new UnionFind<>();
    private final Map<Vertex, EqClass> equivalence = new HashMap<>();

    private final CausalityManager causality = new CausalityManager();
    private final VertexFactory vertexFactory = new VertexFactory();

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
        unionFind.add(vertex);
        causality.init(vertex);
        equivalence.put(vertex, new EqClass(vertex));

        inEdges.putIfAbsent(vertex, new LinkedList<>());
        outEdges.putIfAbsent(vertex, new LinkedList<>());
    }

    public void addEdge(Vertex from, Vertex to) {
        outEdges.get(from).add(to);
        inEdges.get(to).add(from);
        causality.addEdge(from, to);
    }

    public boolean equivalent(Vertex a, Vertex b) {
        return unionFind.find(a) == unionFind.find(b);
    }

    public EqClass eqClassOf(Vertex vertex) {
        return equivalence.get(unionFind.find(vertex));
    }

    public EqClass union(Vertex a, Vertex b) {
        if (!equivalent(a, b)) {
            var set = new EqClass();
            set.union(equivalence.remove(unionFind.find(a)));
            set.union(equivalence.remove(unionFind.find(b)));
            set.canonicalize();
            unionFind.union(a, b);
            causality.union(a, b);
            equivalence.put(unionFind.find(a), set);
        }
        return eqClassOf(a);
    }

    public VertexFactory factory() {
        return vertexFactory;
    }

    public List<Vertex> inputsOf(Vertex vertex) {
        return Collections.unmodifiableList(inEdges.getOrDefault(vertex, List.of()));
    }

    public List<Vertex> mutInputsOf(Vertex vertex) {
        return inEdges.getOrDefault(vertex, new LinkedList<>());
    }

    public List<Vertex> outputsOf(Vertex vertex) {
        return Collections.unmodifiableList(outEdges.getOrDefault(vertex, List.of()));
    }

    public Vertex eqRepOf(Vertex vertex) {
        return unionFind.find(vertex);
    }

    public Collection<Component> components() {
        return vertices.stream().filter(Component.class::isInstance).map(Component.class::cast).toList();
    }

    public Collection<Vertex> vertices() {
        return Collections.unmodifiableSet(vertices);
    }

    public Collection<Vertex> mutVertices() {
        return vertices;
    }

    public Map<Vertex, List<Vertex>> equivalenceClasses() {
        Map<Vertex, List<Vertex>> result = new LinkedHashMap<>();
        for (var v : vertices) {
            var rep = unionFind.find(v);
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
            var rep = unionFind.find(v);
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

    public static final class CausalityManager {
        private final Map<Vertex, Set<Vertex>> preds = new HashMap<>();
        private final Map<Vertex, Set<Vertex>> succs = new HashMap<>();

        void init(Vertex vertex) {
            preds.put(vertex, new HashSet<>(Set.of(vertex)));
            succs.put(vertex, new HashSet<>(Set.of(vertex)));
        }

        void addEdge(Vertex from, Vertex to) {
            for (var vertex : preds.get(from)) {
                succs.get(vertex).addAll(succs.get(to));
            }
            for (var vertex : succs.get(to)) {
                preds.get(vertex).addAll(preds.get(from));
            }
        }

        void union(Vertex u, Vertex v) {
            preds.get(u).addAll(preds.get(v));
            succs.get(u).addAll(succs.get(v));
            preds.get(v).addAll(preds.get(u));
            succs.get(v).addAll(succs.get(u));
        }

        boolean test(Vertex u, Vertex v) {
            return succs.get(u).contains(v);
        }
    }

    public class EqClass implements Iterable<Vertex> {
        private final Set<Vertex> elements;

        EqClass(Vertex element) {
            this.elements = new HashSet<>(Set.of(element));
        }

        EqClass() {
            this.elements = new HashSet<>();
        }

        void union(EqClass other) {
            elements.addAll(other.elements);
        }

        void canonicalize() {
            if (elements.size() <= 1) return;
            List<Vertex> toRemove = new LinkedList<>();
            for (var v : elements) {
                for (var u : elements) {
                    if (u == v) continue;
                    if (causality.test(u, v) && Vertices.isCombinational(v)) {
                        toRemove.add(v);
                        break;
                    }
                }
            }
            toRemove.forEach(elements::remove);
        }

        @Override
        @Nonnull
        public Iterator<Vertex> iterator() {
            return elements.iterator();
        }

        public Stream<Vertex> stream() {
            return elements.stream();
        }

        public Vertex getAny() {
            return elements.iterator().next();
        }

        public void remove(Vertex v) {
            elements.remove(v);
        }

        public <T extends Vertex> boolean contains(Class<T> cls, Predicate<T> predicate) {
            for (var vertex : elements) {
                if (cls.isInstance(vertex)) {
                    if (predicate.test(cls.cast(vertex))) {
                        return true;
                    }
                }
            }
            return false;
        }

        public <T extends Vertex> List<T> filter(Class<T> cls, Predicate<T> predicate) {
            List<T> result = new LinkedList<>();
            for (var vertex : elements) {
                if (cls.isInstance(vertex)) {
                    if (predicate.test(cls.cast(vertex))) {
                        result.add(cls.cast(vertex));
                    }
                }
            }
            return result;
        }
    }

    public final class VertexFactory {
        private final Map<BitVector, Constant> constantCache = new HashMap<>();
        private final AtomicInteger indexer = new AtomicInteger();

        private int nextIndex() {
            return indexer.getAndIncrement();
        }

        public Wire wire(String label, int width) {
            var wire = new Wire(nextIndex(), label, width);
            add(wire);
            return wire;
        }
        public WriteSelect writeSelect(String label, Selector selector) {
            var select = new WriteSelect(nextIndex(), label, selector);
            add(select);
            return select;
        }

        public ReadSelect readSelect(Selector selector) {
            var select = new ReadSelect(nextIndex(), selector);
            add(select);
            return select;
        }

        public Component component(Component.Label label, int width) {
            var component = new Component(nextIndex(), label, width);
            add(component);
            return component;
        }

        public Component component(String label, int width) {
            var component = new Component(nextIndex(), label, width);
            add(component);
            return component;
        }

        public Constant constant(BitVector value) {
            if (constantCache.containsKey(value)) {
                return constantCache.get(value);
            }
            var constant = new Constant(nextIndex(), value);
            constantCache.put(value, constant);
            add(constant);
            return constant;
        }
    }
}

