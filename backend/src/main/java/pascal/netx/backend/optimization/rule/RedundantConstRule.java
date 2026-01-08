package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Constant;
import pascal.netx.backend.optimization.model.Graph;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public final class RedundantConstRule extends UniRule {
    public RedundantConstRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        return switch (vertex.label()) {
            case NAND, AND -> rewriteConstant(vertex, Constant::isMersenne);
            case NOR, XNOR, OR, XOR -> rewriteConstant(vertex, Constant::isZero);
            default -> false;
        };
    }

    private boolean rewriteConstant(Component vertex, Predicate<Constant> predicate) {
        boolean should = false;
        List<Graph.EqClass> newInputs = new ArrayList<>();
        for (var input : graph.inputsOf(vertex)) {
            if (input.contains(Constant.class, predicate)) {
                should = true;
            } else {
                newInputs.add(input);
            }
        }
        if (should) {
            var dual = graph.factory().component(vertex.label(), vertex.outWidth());
            newInputs.forEach(v -> graph.addEdge(v, dual));
            graph.replace(vertex, dual);
            markSeen(vertex);
        }
        return should;
    }
}
