package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.HashSet;

public abstract class BidiMemRule extends GraphRule {
    private final HashSet<Vertex> pulled = new HashSet<>();
    private final HashSet<Vertex> pushed = new HashSet<>();

    protected BidiMemRule(Graph graph) {
        super(graph);
    }

    protected final boolean hasPulled(Vertex vertex) {
        return pulled.contains(vertex);
    }

    protected final boolean hasPushed(Vertex vertex) {
        return pushed.contains(vertex);
    }

    protected final void markPulled(Vertex vertex) {
        pulled.add(vertex);
    }

    protected final void markPushed(Vertex vertex) {
        pushed.add(vertex);
    }

    @Override
    public boolean apply(Component vertex) {
        boolean changed = false;
        changed |= push(vertex);
        changed |= pull(vertex);
        return changed;
    }

    abstract boolean push(Component vertex);

    abstract boolean pull(Component vertex);
}
