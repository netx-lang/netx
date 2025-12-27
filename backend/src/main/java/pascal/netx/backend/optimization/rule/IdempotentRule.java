package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Graph;

public final class IdempotentRule extends StatelessRule {
    public IdempotentRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        return switch (vertex.label()) {
            case AND, OR, XOR, NAND, NOR, XNOR -> {
                var inputs = graph.inputsOf(vertex);
                var seen = inputs.stream().distinct().toList();
                if (seen.size() < inputs.size()) {
                    inputs.clear();
                    inputs.addAll(seen);
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }
}
