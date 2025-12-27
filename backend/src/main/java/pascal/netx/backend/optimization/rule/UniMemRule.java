package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.HashSet;

public abstract class UniMemRule extends GraphRule {
    private final HashSet<Vertex> rewritten = new HashSet<>();

    protected UniMemRule(Graph graph) {
        super(graph);
    }

    protected final boolean hasSeen(Component vertex) {
        return rewritten.contains(vertex);
    }

    protected final void markSeen(Component vertex) {
        rewritten.add(vertex);
    }
}
