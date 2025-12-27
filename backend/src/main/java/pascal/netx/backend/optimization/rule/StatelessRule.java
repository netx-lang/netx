package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Graph;

public abstract class StatelessRule extends GraphRule {
    protected StatelessRule(Graph graph) {
        super(graph);
    }
}
