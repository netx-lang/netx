package pascal.netx.backend.optimization;

import pascal.netx.backend.optimization.model.Graph;

public final class Rewriter {
    private final Graph graph;

    public Rewriter(Graph graph) {
        this.graph = graph;
    }

    public void rewrite() {

    }

    public void rewrite_constant() {
        for (var vertex : graph.vertices()) {

        }
    }
}
