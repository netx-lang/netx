package pascal.netx.backend.optimization.rule;

import pascal.netx.backend.optimization.model.*;
import pascal.netx.util.lang.BitVector;

import java.util.List;

public final class ConstantPropRule extends UniRule {
    public ConstantPropRule(Graph graph) {
        super(graph);
    }

    @Override
    public boolean apply(Component vertex) {
        if (hasSeen(vertex)) {
            return false;
        }

        if (vertex instanceof Register reg) {
            var input = graph.inputsOf(reg).get(1);
            if (input.contains(Constant.class)) {
                graph.manager().replace(reg, input.filter(Constant.class).getFirst());
                markSeen(vertex);
                return true;
            }
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
                case CAST -> inputs.getFirst().sext(vertex.outWidth());
                case UCAST -> inputs.getFirst().uext(vertex.outWidth());
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
                markSeen(vertex);
                return true;
            }
        }
        return false;
    }
}
