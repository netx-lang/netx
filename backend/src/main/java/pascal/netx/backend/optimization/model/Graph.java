package pascal.netx.backend.optimization.model;

import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.exception.ShallNotReachHereError;
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

    private final Map<Vertex, List<EqClass>> inEdges = new HashMap<>();
    private final Map<EqClass, List<Vertex>> outEdges = new HashMap<>();

    private final VertexManager veq = new VertexManager();
    private final VertexFactory vFact = new VertexFactory();

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

    public void retain(Set<Vertex> vertices) {
        veq.retain(vertices);
    }

    private void add(Vertex vertex) {
        veq.register(vertex);
        inEdges.putIfAbsent(vertex, new LinkedList<>());
        outEdges.putIfAbsent(veq.resolve(vertex), new LinkedList<>());
    }

    public void addEdge(Vertex from, Vertex to) {
        inEdges.get(to).add(veq.resolve(from));
        outEdges.get(veq.resolve(from)).add(to);
    }

    public void addEdge(EqClass from, Vertex to) {
        inEdges.get(to).add(from);
        outEdges.get(from).add(to);
    }

    public EqClass eqClassOf(Vertex vertex) {
        return veq.resolve(vertex);
    }

    public EqClass union(Vertex a, Vertex b) {
        if (!veq.equivalent(a, b)) {
            veq.union(a, b);
        }
        return eqClassOf(a);
    }

    public EqClass union(Vertex a, EqClass e) {
        if (!e.elements.contains(a)) {
            veq.union(a, e);
        }
        return eqClassOf(a);
    }

    public VertexFactory factory() {
        return vFact;
    }

    public List<EqClass> inputsOf(Vertex vertex) {
        return Collections.unmodifiableList(inEdges.getOrDefault(vertex, List.of()));
    }

    public List<Vertex> outputsOf(Vertex vertex) {
        return Collections.unmodifiableList(outEdges.getOrDefault(veq.resolve(vertex), List.of()));
    }

    public Collection<Component> components() {
        return veq.vertices.stream().filter(Component.class::isInstance).map(Component.class::cast).toList();
    }

    public Collection<Vertex> vertices() {
        return Collections.unmodifiableSet(veq.vertices);
    }

    public void dump(NotepadWriter writer) {
        var classes = new ArrayList<>(veq.vertices.stream().map(this::eqClassOf).distinct().toList());
        var ordered = new ArrayList<>(classes.stream().flatMap(EqClass::stream).toList());

        ordered.sort(Comparator.comparingInt(Vertex::index));

        writer.println("digraph G {");
        writer.incIndent();

        // Emit clusters for equivalence classes with more than one member
        int clusterIdx = 0;
        for (var cls : classes) {
            // avoid embedding '\n' in format strings: print then newline
            writer.printf("subgraph cluster_%d {", clusterIdx++);
            writer.newLine();
            writer.incIndent();
            writer.println("style=dashed;");
            for (var v : cls) {
                var name = "v" + v.index();
                var label = quote(v.name());
                writer.printf("%s [label=%s];", name, label);
                writer.newLine();
            }
            writer.decIndent();
            writer.println("}");
            writer.newLine();
        }

        writer.newLine();

        for (var v : ordered) {
            var fromName = "v" + v.index();
            var targets = inEdges.getOrDefault(v, List.of());
            for (var cls : targets) {
                var toName = "v" + cls.getAny().index();
                writer.printf("%s -> %s;", toName, fromName);
                writer.newLine();
            }
        }

        writer.decIndent();
        writer.println("}");
    }

    private final class VertexManager {
        private final Set<Vertex> vertices = new HashSet<>();
        private final UnionFind<Vertex> unionFind = new UnionFind<>();
        private final Map<Vertex, EqClass> equivalence = new HashMap<>();

        void register(Vertex vertex) {
            vertices.add(vertex);
            unionFind.add(vertex);
            equivalence.put(vertex, new EqClass(vertex));
        }

        boolean equivalent(Vertex u, Vertex v) {
            return unionFind.find(u) == unionFind.find(v);
        }

        void retain(Set<Vertex> retained) {
            vertices.retainAll(retained);
            equivalence.values().forEach(cls -> cls.retain(vertices));
        }

        void union(Vertex u, Vertex v) {
            var set = new EqClass();
            var uCls = equivalence.remove(unionFind.find(u));
            var vCls = equivalence.remove(unionFind.find(v));

            set.union(uCls);
            set.union(vCls);

            inEdges.values().forEach(list -> list.replaceAll(cls -> (cls == uCls || cls == vCls) ? set : cls));

            outEdges.put(set, new LinkedList<>());
            outEdges.get(set).addAll(outEdges.getOrDefault(uCls, List.of()));
            outEdges.get(set).addAll(outEdges.getOrDefault(vCls, List.of()));
            outEdges.remove(uCls);
            outEdges.remove(vCls);

            unionFind.union(u, v);
            equivalence.put(unionFind.find(u), set);
        }

        void union(Vertex u, EqClass e) {
            var set = new EqClass();
            set.union(equivalence.remove(unionFind.find(u)));
            set.union(e);
            unionFind.union(u, e.getAny());
            equivalence.put(unionFind.find(u), set);
        }

        EqClass resolve(Vertex vertex) {
            return equivalence.get(unionFind.find(vertex));
        }
    }

    public static class EqClass implements Iterable<Vertex> {
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

        public void retain(Set<Vertex> retained) {
            elements.retainAll(retained);
        }

        @Override
        @Nonnull
        public Iterator<Vertex> iterator() {
            return elements.iterator();
        }

        public Stream<Vertex> stream() {
            return elements.stream();
        }

        private Vertex getAny() {
            return elements.iterator().next();
        }

        public int width() {
            return getAny().outWidth();
        }

        public void remove(Vertex v) {
//            elements.remove(v);
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

        @Override
        public boolean equals(Object o) {
            return this == o;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(elements);
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

