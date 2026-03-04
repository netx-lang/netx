package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.lang.BitVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static pascal.netx.backend.optimization.model.Component.Label.CAST;
import static pascal.netx.backend.optimization.model.Component.Label.CONCAT;

public final class ConstantPropRule extends UniRule {
    public ConstantPropRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (vertex instanceof Register reg) {
            var input = graph.inputsOf(reg).get(1);
            if (input.contains(Constant.class)) {
                graph.manager().replace(reg, input.filter(Constant.class).getFirst());
                return true;
            }
            return false;
        }

        boolean allConst = true;
        for (var input : graph.inputsOf(vertex)) {
            if (!input.contains(Constant.class)) {
                allConst = false;
                break;
            }
        }

        if (allConst) {
            List<BitVector> inputs = graph.inputsOf(vertex).stream()
                    .map(cls -> cls.filter(Constant.class)).map(List::getFirst)
                    .map(Constant::value).toList();

            BitVector result = switch (vertex.label()) {
                case CAST -> cast(inputs.getFirst(), true);
                case UCAST -> cast(inputs.getFirst(), false);
                case TO -> inputs.getFirst();
                case CONCAT -> inputs.stream().reduce(BitVector::concat).orElseThrow();
                case SELECT -> {
                    assert vertex instanceof ReadSelect;
                    var selector = ((ReadSelect) vertex).selector();
                    yield inputs.getFirst().select(selector.msb(), selector.lsb());
                }
                case OR -> inputs.stream().reduce(BitVector::or).orElseThrow();
                case AND -> inputs.stream().reduce(BitVector::and).orElseThrow();
                case XOR -> inputs.stream().reduce(BitVector::xor).orElseThrow();
                case NOT -> inputs.getFirst().not();
                case NOR -> inputs.stream().reduce(BitVector::or).orElseThrow().not();
                case NAND -> inputs.stream().reduce(BitVector::and).orElseThrow().not();
                case XNOR -> inputs.stream().reduce(BitVector::xor).orElseThrow().not();
                case EQ -> BitVector.fromBoolean(inputs.get(0).eq(inputs.get(1)));
                case NE -> BitVector.fromBoolean(!inputs.get(0).eq(inputs.get(1)));
                case SLT -> BitVector.fromBoolean(inputs.get(0).slt(inputs.get(1)));
                case SLE -> BitVector.fromBoolean(inputs.get(0).sle(inputs.get(1)));
                case SGT -> BitVector.fromBoolean(inputs.get(0).sgt(inputs.get(1)));
                case SGE -> BitVector.fromBoolean(inputs.get(0).sge(inputs.get(1)));
                case ULT -> BitVector.fromBoolean(inputs.get(0).ult(inputs.get(1)));
                case ULE -> BitVector.fromBoolean(inputs.get(0).ule(inputs.get(1)));
                case UGT -> BitVector.fromBoolean(inputs.get(0).ugt(inputs.get(1)));
                case UGE -> BitVector.fromBoolean(inputs.get(0).uge(inputs.get(1)));
                case ADD -> inputs.stream().reduce(BitVector::add).orElseThrow();
                case SUB -> inputs.get(0).sub(inputs.get(1));
                case SHL -> inputs.get(0).shl(inputs.get(1).toUInt());
                case MUL -> inputs.stream().reduce(BitVector::mul).orElseThrow();
                case DIV -> inputs.get(0).div(inputs.get(1));
                case MOD -> inputs.get(0).mod(inputs.get(1));
                case ASHR -> inputs.get(0).ashr(inputs.get(1).toUInt());
                case LSHR -> inputs.get(0).lshr(inputs.get(1).toUInt());
                default -> null;
            };
            if (result != null) {
                var constVertex = graph.factory().constant(result);
                graph.manager().replace(vertex, constVertex);
                return true;
            }
        } else {
            return switch (vertex.label()) {
                case SHL, ASHR, LSHR -> propagateShift(vertex);
                case OR -> propagateAbsorbing(vertex, Constant::isMersenne, false);
                case NOR -> propagateAbsorbing(vertex, Constant::isMersenne, true);
                case NAND -> propagateAbsorbing(vertex, Constant::isZero, false);
                case MUL -> propagateAbsorbing(vertex, Constant::isZero, true)
                        || rewriteConstMultiply(vertex);
                case AND -> propagateAbsorbing(vertex, Constant::isZero, true);
                default -> false;
            };
        }
        return false;
    }

    private boolean rewriteConstMultiply(Component vertex) {
        if (vertex.label() != Component.Label.MUL || hasMarker(vertex, Marker.ConstMulti)) {
            return false;
        }

        Set<Graph.EqClass> inputs = new HashSet<>(graph.inputsOf(vertex));
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

        long coef = constants.stream().map(Constant::value).reduce(BitVector::mul).orElseThrow().toULong();
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

            long rem = coef;
            var shiftDual = graph.factory().component(Component.Label.ADD, vertex.outWidth());
            while (rem > 0) {
                var shiftAmount = Long.numberOfTrailingZeros(rem);
                if (shiftAmount == 0) {
                    graph.addEdge(variable, shiftDual);
                    rem &= ~1L;
                    continue;
                }
                var subShift = graph.factory().component(Component.Label.SHL, vertex.outWidth());
                var shiftConst = graph.factory().constant(BitVector.fromLong(vertex.outWidth(), shiftAmount));
                graph.addEdge(variable, subShift);
                graph.addEdge(shiftConst, subShift);
                graph.addEdge(subShift, shiftDual);
                rem &= ~(1L << shiftAmount);
            }
            graph.manager().union(shiftDual, vertex);
        }
        return true;
    }

    private static boolean isPowerOfTwo(long x) {
        return (x & (x - 1)) == 0 && x > 0;
    }

    private boolean propagateAbsorbing(Component vertex, Predicate<Constant> matcher, boolean zero) {
        boolean replace = graph.inputsOf(vertex).stream().anyMatch(cls -> cls.contains(Constant.class, matcher));
        if (replace) {
            var val = BitVector.fromLong(vertex.outWidth(), 0L);
            if (!zero) val = val.not();
            var result = graph.factory().constant(val);
            graph.manager().replace(vertex, result);
        }
        return replace;
    }

    private boolean propagateShift(Component vertex) {
        var amountCls = graph.inputsOf(vertex).get(1);
        if (!amountCls.contains(Constant.class)) {
            return false;
        }
        int amount = amountCls.filter(Constant.class).getFirst().value().toUInt();
        if (amount == 0) {
            var valueCls = graph.inputsOf(vertex).getFirst();
            graph.manager().replace(vertex, valueCls.getAny());
        } else {
            int width = vertex.outWidth();
            var input = graph.inputsOf(vertex).getFirst();
            var dual = graph.factory().component(CONCAT, width);
            switch (vertex.label()) {
                case SHL -> {
                    var select = graph.factory().readSelect(new Selector(width - amount - 1, 0));
                    var zeros = graph.factory().constant(BitVector.fromInt(amount, 0));
                    graph.addEdge(input, select);
                    graph.addEdge(select, dual);
                    graph.addEdge(zeros, dual);
                }
                case LSHR -> {
                    var select = graph.factory().readSelect(new Selector(width - 1, amount));
                    var zeros = graph.factory().constant(BitVector.fromInt(amount, 0));
                    graph.addEdge(input, select);
                    graph.addEdge(zeros, dual);
                    graph.addEdge(select, dual);
                }
                case ASHR -> {
                    var select = graph.factory().readSelect(new Selector(width - 1, amount));
                    var signSelect = graph.factory().readSelect(new Selector(width - 1, width - 1));
                    var signs = graph.factory().component(CAST, amount);
                    graph.addEdge(input, signSelect);
                    graph.addEdge(input, select);
                    graph.addEdge(signSelect, signs);

                    graph.addEdge(signs, dual);
                    graph.addEdge(select, dual);
                }
            }
            graph.manager().replace(vertex, dual);
        }
        return true;
    }

    private BitVector cast(BitVector value, boolean signed) {
        int inW = value.size();
        int outW = value.size();
        if (inW == outW) {
            return value;
        } else if (inW < outW) {
            if (signed) {
                return value.sext(outW);
            } else {
                return value.uext(outW);
            }
        } else {
            return value.truncate(outW);
        }
    }
}
