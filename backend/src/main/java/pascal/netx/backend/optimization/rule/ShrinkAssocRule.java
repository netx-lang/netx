package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.LinkedList;
import java.util.List;

public final class ShrinkAssocRule extends StatelessRule {
    public ShrinkAssocRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        switch (vertex.label()) {
            case AND, OR, XOR, ADD, MUL -> {
                boolean changed = false;
                List<Vertex> flattened = new LinkedList<>();
                List<Vertex> toRemove = new LinkedList<>();
                for (var input : graph.inputsOf(vertex)) {
                    if (input instanceof Component comp
                            && comp.label() == vertex.label()
                            && graph.outputsOf(comp).size() == 1) {
                        apply(comp);
                        flattened.addAll(graph.inputsOf(comp));
                        toRemove.add(comp);
                        changed = true;
                    } else {
                        flattened.add(input);
                    }
                }
                if (changed) {
                    graph.inputsOf(vertex).clear();
                    graph.inputsOf(vertex).addAll(flattened);
                    toRemove.forEach(graph::remove);
                    return true;
                }
            }
        }
        return false;
    }
}
