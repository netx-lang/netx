package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

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
            case REGISTER, ASYNC_REGISTER, CAST, NOT, TO -> false;
            default -> {
                var inputs = graph.inputsOf(vertex);
                if (inputs.size() == 1) {
                    var only = inputs.getFirst();
                    graph.union(vertex, only);
                    markSeen(vertex);
                    yield true;
                }
                yield false;
            }
        };
    }
}
