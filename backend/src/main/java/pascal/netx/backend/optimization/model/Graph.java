package pascal.netx.backend.optimization.model;

import pascal.netx.util.collection.UnionFind;
import pascal.netx.util.print.NotepadWriter;

import java.util.*;

import static pascal.netx.backend.NxonParseUtils.quote;

public final class Graph {
    private final Set<Vertex> vertices = new HashSet<>();
    private final Map<Vertex, List<Vertex>> adjacency = new HashMap<>();

    private final UnionFind<Vertex> equivalence = new UnionFind<>();

    public void add(Vertex vertex) {
        vertices.add(vertex);
        equivalence.add(vertex);
        adjacency.putIfAbsent(vertex, new ArrayList<>());
    }

    public void addEdge(Vertex from, Vertex to) {
        adjacency.get(from).add(to);
    }

    public void union(Vertex a, Vertex b) {
        equivalence.union(a, b);
    }

    public void dump(NotepadWriter writer) {
        var ordered = new ArrayList<>(vertices);
        ordered.sort(Comparator.comparingInt(Vertex::index));

        // Group vertices by their union-find representative in a stable order
        Map<Vertex, List<Vertex>> groups = new LinkedHashMap<>();
        for (var v : ordered) {
            var rep = equivalence.find(v);
            groups.computeIfAbsent(rep, k -> new ArrayList<>()).add(v);
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
                var label = quote(v.label());
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
            var label = quote(v.label());
            writer.printf("%s [label=%s];", name, label);
            writer.newLine();
        }

        writer.newLine();

        // Emit edges
        for (var v : ordered) {
            var fromName = "v" + v.index();
            var targets = adjacency.getOrDefault(v, List.of());
            for (var t : targets) {
                var toName = "v" + t.index();
                writer.printf("%s -> %s;", fromName, toName);
                writer.newLine();
            }
        }

        writer.decIndent();
        writer.println("}");
    }
}
