package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public abstract class BidiRule extends GraphRule {
    protected BidiRule(Graph graph) {
        super(graph);
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
