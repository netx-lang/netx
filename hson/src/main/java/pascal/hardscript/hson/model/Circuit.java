package pascal.hardscript.hson.model;

import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.List;

public class Circuit {
    private final DataManager manager = new DataManager();
    private final Graph<Node, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

    public Node addVertex(Node node) {
        graph.addVertex(node);
        return manager.addNode(node);
    }

    public void addHyperEdge(Identifier id, List<Node> from, List<Node> to) {
        var node = addVertex(new ComputationNode(id));
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

        public Node addNode(Node node) {
            var id = node.getIdentifier();
            nodes.computeIfAbsent(id.getSourceLoc(), _ -> new IdentifierMap()).put(id, node);
            return node;
        }

        public Node resolve(Identifier identifier) {
            return nodes.get(identifier.getSourceLoc()).get(identifier);
        }
    }
}
