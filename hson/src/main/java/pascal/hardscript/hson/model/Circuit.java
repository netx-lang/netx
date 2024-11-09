package pascal.hardscript.hson.model;

import org.jgrapht.Graph;
import org.jgrapht.Graphs;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.jgrapht.traverse.TopologicalOrderIterator;

import java.util.*;

public class Circuit {
    private final List<DataNode> inputs = new LinkedList<>();
    private final List<DataNode> outputs = new LinkedList<>();
    private final Map<ClockNode, List<Register>> registers = new HashMap<>();
    private final Graph<Node, DefaultEdge> graph = new SimpleDirectedGraph<>(DefaultEdge.class);

    public void addVertex(Node node) {
        graph.addVertex(node);
    }

    public void addRegister(Register register) {
        registers.computeIfAbsent(register.getClock(), _ -> new LinkedList<>()).add(register);
    }

    public void addHyperEdge(Node node, List<DataNode> from, List<DataNode> to) {
        addVertex(node);
        from.forEach(f -> addEdge(f, node));
        to.forEach(t -> addEdge(node, t));
    }

    public void addEdge(Node from, Node to) {
        graph.addEdge(from, to);
    }

    public List<DataNode> getInputs() {
        return inputs;
    }

    public List<DataNode> getOutputs() {
        return outputs;
    }

    public void addInput(DataNode node) {
        inputs.add(node);
    }

    public void addOutput(DataNode node) {
        outputs.add(node);
    }

    public Map<ClockNode, List<Register>> getRegisters() {
        return registers;
    }

    public Set<Node> getAllNodes() {
        return graph.vertexSet();
    }

    public void topologySort() {
        int order = 0;
        var iterator = new TopologicalOrderIterator<>(graph);
        while (iterator.hasNext()) {
            iterator.next().setOrder(order++);
        }
    }

    public List<Node> getPredecessors(Node node) {
        return Graphs.predecessorListOf(graph, node);
    }

    public List<Node> getSuccessors(Node node) {
        return Graphs.successorListOf(graph, node);
    }
}
