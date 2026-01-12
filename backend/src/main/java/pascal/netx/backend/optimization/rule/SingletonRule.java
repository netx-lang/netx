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
        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER, CAST, UCAST, NOT, TO, SELECT -> false;
            default -> {
                var inputs = graph.inputsOf(vertex);
                if (inputs.size() == 1) {
                    var only = inputs.getFirst();
                    Vertex v = only.getAny();
                    graph.manager().replace(vertex, v);
                    yield true;
                }
                yield false;
            }
        };
    }
}
