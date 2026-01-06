package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Graph;

public abstract class GraphRule implements Rule {
    protected final Graph graph;

    protected GraphRule(Graph graph) {
        this.graph = graph;
    }

}

