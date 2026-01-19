package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.Component;
import pascal.netx.backend.optimization.model.Constant;
import pascal.netx.backend.optimization.model.Graph;
import pascal.netx.util.lang.BitVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConstMultiRule extends UniRule {
    public ConstMultiRule(Graph graph) {
        super(graph);
    }

    private static boolean isPowerOfTwo(long x) {
        return (x & (x - 1)) == 0 && x > 0;
    }

    @Override
    public boolean apply(Component vertex) {
        if (!(vertex.label() == Component.Label.MUL)) {
            return false;
        }
        if (hasMarker(vertex, Marker.ConstMulti)) {
            return false;
        }

        Set<Graph.EqClass> inputs = new HashSet<>(Set.copyOf(graph.inputsOf(vertex)));
        List<Constant> constants = new ArrayList<>();
        var iter = inputs.iterator();
        while (iter.hasNext()) {
            var cls = iter.next();
            if (cls.contains(Constant.class)) {
                constants.add(cls.filter(Constant.class).getFirst());
                iter.remove();
            }
        }

        if (constants.isEmpty() || inputs.isEmpty()) {
            return false;
        }

        Graph.EqClass variable;
        if (inputs.size() == 1) {
            variable = inputs.iterator().next();
        } else {
            var mid = graph.factory().component(Component.Label.MUL, vertex.outWidth());
            inputs.forEach(cls -> graph.addEdge(cls, mid));
            variable = graph.eqClassOf(mid);
        }
        var coef = constants.stream().map(Constant::value).reduce(BitVector::mul).orElseThrow().toULong();

        mark(vertex, Marker.ConstMulti);
        if (coef == 0) {
            var zeroConst = graph.factory().constant(BitVector.fromLong(vertex.outWidth(), 0L));
            graph.manager().replace(vertex, zeroConst);
        } else if (isPowerOfTwo(coef)) {
            var shiftDual = graph.factory().component(Component.Label.SHL, vertex.outWidth());
            int shiftAmount = Long.numberOfTrailingZeros(coef);
            var shiftConst = graph.factory().constant(BitVector.fromLong(vertex.outWidth(), shiftAmount));
            graph.addEdge(variable, shiftDual);
            graph.addEdge(shiftConst, shiftDual);
            graph.manager().union(shiftDual, vertex);
        } else if (coef < 20) {
            var repeatedDual = graph.factory().component(Component.Label.ADD, vertex.outWidth());
            for (int i = 0; i < coef; i++) {
                graph.addEdge(variable, repeatedDual);
            }
            graph.manager().union(repeatedDual, vertex);

            var shiftDual = graph.factory().component(Component.Label.ADD, vertex.outWidth());
            while (coef > 0) {
                var shiftAmount = Long.numberOfTrailingZeros(coef);
                if (shiftAmount == 0) {
                    graph.addEdge(variable, shiftDual);
                    coef &= ~1L;
                    continue;
                }
                var subShift = graph.factory().component(Component.Label.SHL, vertex.outWidth());
                var shiftConst = graph.factory().constant(BitVector.fromLong(vertex.outWidth(), shiftAmount));
                graph.addEdge(variable, subShift);
                graph.addEdge(shiftConst, subShift);
                graph.addEdge(subShift, shiftDual);
                coef &= ~(1L << shiftAmount);
            }
            graph.manager().union(shiftDual, vertex);
        }
        return true;
    }
}
