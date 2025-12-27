package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Constant;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.backend.optimization.model.Vertex;

import java.util.function.Predicate;

public final class IdentityRule extends StatelessRule {
    public IdentityRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        return switch (vertex.label()) {
            case AND -> rewriteConstant(vertex, Constant::isMersenne);
            case OR -> rewriteConstant(vertex, Constant::isZero);
            default -> false;
        };
    }

    private boolean rewriteConstant(Vertex vertex, Predicate<Constant> predicate) {
        return graph.inputsOf(vertex).removeIf(v -> v instanceof Constant constant && predicate.test(constant));
    }
}
