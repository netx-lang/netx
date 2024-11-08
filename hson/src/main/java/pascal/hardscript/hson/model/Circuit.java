package pascal.hardscript.hson.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class Circuit {
    private final DataManager manager = new DataManager();
    private final Map<ClockNode, List<RegisterNode>> registers = new HashMap<>();
    private final Graph<Node, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

    public void addVertex(Node node) {
        switch (node) {
            case ClockNode clock -> registers.put(clock, new LinkedList<>());
            case RegisterNode register -> registers.get(register.clock()).add(register);
            default -> {
                // do nothing
            }
        }
        graph.addVertex(node);
        manager.addNode(node);
    }

    public void addHyperEdge(RelationNode node, List<Node> from, List<Node> to) {
        addVertex(node);
        from.forEach(f -> addEdge(f, node));
        to.forEach(t -> addEdge(node, t));
    }

    private void addEdge(Node from, Node to) {
        graph.addEdge(from, to);
    }

    @Nonnull
    public Node resolve(Identifier identifier) {
        return manager.resolve(identifier);
    }

    private static class DataManager {
        private static class IdentifierMap extends HashMap<Identifier, Node> {}
        private final HashMap<SourceLoc, IdentifierMap> nodes = new HashMap<>();

        public void addNode(Node node) {
            var id = node.getIdentifier();
            nodes.computeIfAbsent(id.getSourceLoc(), _ -> new IdentifierMap()).put(id, node);
        }

        public Node resolve(Identifier identifier) {
            return nodes.get(identifier.getSourceLoc()).get(identifier);
        }
    }
}
