package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class SingletonRule extends StatelessRule {
    public SingletonRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        return switch (vertex.label()) {
            case REGISTER, ASYNC_REGISTER, CAST, NOT, TO -> false;
            default -> {
                var inputs = graph.inputsOf(vertex);
                if (inputs.size() == 1) {
                    var only = inputs.getFirst();
                    graph.replaceAndRemove(vertex, only);
                    yield true;
                }
                yield false;
            }
        };
    }
}
