package pascal.netx.backend.optimization.model;

import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.print.NotepadWriter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static pascal.netx.backend.NxonParseUtils.quote;

public final class Graph {
    private final List<Vertex> globalIn = new ArrayList<>();
    private final List<Vertex> globalOut = new ArrayList<>();

    private final Map<Vertex, List<EqClass>> inEdges = new HashMap<>();
    private final VertexManager manager = new VertexManager();
    private final VertexFactory factory = new VertexFactory();

    private final Map<Vertex, BitSet> rewriteRecord = new HashMap<>();

    private void add(Vertex vertex) {
        manager.register(vertex);
        inEdges.putIfAbsent(vertex, new LinkedList<>());
    }

    public void setGlobalIn(List<Vertex> globalIn) {
        this.globalIn.addAll(globalIn);
    }

    public void setGlobalOut(List<Vertex> globalOut) {
        this.globalOut.addAll(globalOut);
    }

    public List<Vertex> globalOut() {
        return globalOut;
    }

    public List<Vertex> globalIn() {
        return globalIn;
    }

    public void retain(Set<Vertex> vertices) {
        manager.retain(vertices);
    }

    public void addEdge(Vertex from, Vertex to) {
        inEdges.get(to).add(manager.resolve(from));
    }

    public void addEdge(EqClass from, Vertex to) {
        inEdges.get(to).add(from);
    }

    public EqClass eqClassOf(Vertex vertex) {
        return manager.resolve(vertex);
    }

    public Collection<EqClass> eqClasses() {
        return vertices().stream().map(this::eqClassOf).collect(Collectors.toSet());
    }

    public VertexManager manager() {
        return manager;
    }

    public VertexFactory factory() {
        return factory;
    }

    public List<EqClass> inputsOf(Vertex vertex) {
        return Collections.unmodifiableList(inEdges.getOrDefault(vertex, List.of()));
    }

    public Collection<Component> components() {
        return manager.vertices.stream().filter(Component.class::isInstance).map(Component.class::cast).toList();
    }

    public Collection<Vertex> vertices() {
        return Collections.unmodifiableSet(manager.vertices);
    }

    public Map<Vertex, BitSet> rewriteRecord() {
        return rewriteRecord;
    }

    public void dumpDot(NotepadWriter writer) {
        var classes = new ArrayList<>(eqClasses());
        var ordered = new ArrayList<>(vertices());

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

    public static class EqClass implements Iterable<Vertex> {
        private static final AtomicInteger NEXT_ID = new AtomicInteger(0);
        private final int id = NEXT_ID.getAndIncrement();
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

        void remove(Vertex u) {
            elements.remove(u);
        }

        public Vertex getAny() {
            return elements.iterator().next();
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

        public int width() {
            return getAny().outWidth();
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

        public <T extends Vertex> boolean contains(Class<T> cls) {
            for (var vertex : elements) {
                if (cls.isInstance(vertex)) {
                    return true;
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

        public <T extends Vertex> List<T> filter(Class<T> cls) {
            List<T> result = new LinkedList<>();
            for (var vertex : elements) {
                if (cls.isInstance(vertex)) {
                    result.add(cls.cast(vertex));
                }
            }
            return result;
        }

        public boolean isSingleton() {
            return elements.size() == 1;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            EqClass vertices = (EqClass) o;
            return id == vertices.id;
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(id);
        }
    }

    public final class VertexManager {
        private final Set<Vertex> vertices = new HashSet<>();
        private final UnionFind<Vertex> unionFind = new UnionFind<>();
        private final Map<Vertex, EqClass> equivalence = new HashMap<>();

        public void union(Vertex u, Vertex v) {
            if (equivalent(u, v)) {
                return;
            }

            var set = new EqClass();
            var uCls = equivalence.remove(unionFind.find(u));
            var vCls = equivalence.remove(unionFind.find(v));
            set.union(uCls);
            set.union(vCls);

            inEdges.values().forEach(
                    list -> list.replaceAll(
                            cls -> (cls == uCls || cls == vCls) ? set : cls));
            unionFind.union(u, v);
            equivalence.put(unionFind.find(u), set);
        }

        public void replace(Vertex u, Vertex v) {
            union(u, v);
            vertices.remove(u);
            unionFind.replace(u, v);
            equivalence.values().forEach(cls -> cls.remove(u));
            if (equivalence.containsKey(u)) {
                equivalence.put(v, equivalence.remove(u));
            }
        }

        private void register(Vertex vertex) {
            vertices.add(vertex);
            unionFind.add(vertex);
            equivalence.put(vertex, new EqClass(vertex));
        }

        private boolean equivalent(Vertex u, Vertex v) {
            return unionFind.find(u) == unionFind.find(v);
        }

        private void retain(Set<Vertex> retained) {
            vertices.retainAll(retained);
            equivalence.values().forEach(cls -> cls.retain(vertices));
        }

        private EqClass resolve(Vertex vertex) {
            return equivalence.get(unionFind.find(vertex));
        }

        public void canonicalize(Map<Vertex, BitSet> causality) {
            for (var cls : equivalence.values()) {
                var elements = Set.copyOf(cls.elements);
                cls.elements.removeIf(u -> elements.stream().anyMatch(v -> causality.get(u).get(v.index())));
            }
        }
    }

    public final class VertexFactory {
        private final Map<BitVector, Constant> constantCache = new HashMap<>();
        private final AtomicInteger indexer = new AtomicInteger();

        private int nextIndex() {
            return indexer.getAndIncrement();
        }

        public Vertex copy(Vertex vertex) {
            return switch (vertex) {
                case WriteSelect ws -> writeSelect(ws.label(), ws.selector());
                case ReadSelect rs -> readSelect(rs.selector());
                case Register reg -> register(reg.outWidth(), reg.isPosEdge());
                case Component comp -> component(comp.label(), comp.outWidth());
                default -> throw new ShallNotReachHereError();
            };
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
            if (label == Component.Label.REGISTER || label == Component.Label.SELECT) {
                throw new ShallNotReachHereError();
            } else {
                var component = new Component(nextIndex(), label, width);
                add(component);
                return component;
            }
        }

        public Register register(int width, boolean isPosEdge) {
            var register = new Register(nextIndex(), width, isPosEdge);
            add(register);
            return register;
        }

        public Component component(String label, int width) {
            return component(Component.Label.valueOf(label.toUpperCase()), width);
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

