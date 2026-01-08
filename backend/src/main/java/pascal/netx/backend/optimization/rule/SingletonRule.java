package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

public final class SingletonRule extends UniRule {
    public SingletonRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER, CAST, NOT, TO, SELECT -> false;
            default -> {
                var inputs = graph.inputsOf(vertex);
                if (inputs.size() == 1) {
                    var only = inputs.getFirst();
                    Vertex v = only.getAny();
                    graph.manager().replace(vertex, v);
                    markSeen(vertex);
                    yield true;
                }
                yield false;
            }
        };
    }
}
