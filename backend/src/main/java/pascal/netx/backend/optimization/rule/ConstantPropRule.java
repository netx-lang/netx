package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.lang.BitVector;

import java.util.List;
import java.util.Set;

import static pascal.netx.backend.optimization.model.Component.Label.*;

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
            return propagateShift(vertex);
        }
        return false;
    }

    private boolean propagateShift(Component vertex) {
        if (Set.of(SHL, ASHR, LSHR).contains(vertex.label())) {
            var amountCls = graph.inputsOf(vertex).get(1);
            if (amountCls.contains(Constant.class)) {
                int amount = amountCls.filter(Constant.class).getFirst().value().toUInt();
                if (amount == 0) {
                    var valueCls = graph.inputsOf(vertex).getFirst();
                    graph.manager().replace(vertex, valueCls.getAny());
                    return true;
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
            }
        }
        return false;
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
