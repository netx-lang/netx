package pascal.hardscript.hson.simulator;

import pascal.hardscript.util.exception.TodoException;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.BuiltinComponent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;

public class Evaluator {
    private static final Map<BuiltinComponent, BiFunction<BitVector, BitVector, Boolean>> RELATIONAL_OP;
    private static final Map<BuiltinComponent, BiFunction<BitVector, Integer, BitVector>> SHIFT_OP;
    private static final Map<BuiltinComponent, BinaryOperator<BitVector>> BINARY_OP;

    static {
        RELATIONAL_OP = new HashMap<>();
        RELATIONAL_OP.put(BuiltinComponent.EQ, BitVector::eq);
        RELATIONAL_OP.put(BuiltinComponent.NE, BitVector::ne);
        RELATIONAL_OP.put(BuiltinComponent.SLT, BitVector::slt);
        RELATIONAL_OP.put(BuiltinComponent.SLE, BitVector::sle);
        RELATIONAL_OP.put(BuiltinComponent.SGT, BitVector::sgt);
        RELATIONAL_OP.put(BuiltinComponent.SGE, BitVector::sge);
        RELATIONAL_OP.put(BuiltinComponent.ULT, BitVector::ult);
        RELATIONAL_OP.put(BuiltinComponent.ULE, BitVector::ule);
        RELATIONAL_OP.put(BuiltinComponent.UGT, BitVector::ugt);
        RELATIONAL_OP.put(BuiltinComponent.UGE, BitVector::uge);

        BINARY_OP = new HashMap<>();
        BINARY_OP.put(BuiltinComponent.ADD, BitVector::add);
        BINARY_OP.put(BuiltinComponent.SUB, BitVector::sub);
        BINARY_OP.put(BuiltinComponent.OR, BitVector::or);
        BINARY_OP.put(BuiltinComponent.AND, BitVector::and);
        BINARY_OP.put(BuiltinComponent.XOR, BitVector::xor);
        BINARY_OP.put(BuiltinComponent.NOR, BitVector::nor);
        BINARY_OP.put(BuiltinComponent.NAND, BitVector::nand);

        SHIFT_OP = new HashMap<>();
        SHIFT_OP.put(BuiltinComponent.SHL, BitVector::shl);
        SHIFT_OP.put(BuiltinComponent.ASHR, BitVector::ashr);
        SHIFT_OP.put(BuiltinComponent.LSHR, BitVector::lshr);
    };

    public static List<BitVector> evaluate(BuiltinComponent builtin, List<BitVector> inputs) {
        return switch (builtin) {
            case NOT -> evaluateNotGate(inputs);
            case CONCAT -> evaluateMultiIn(BitVector::concat, inputs);
            case ADD, SUB -> evaluateBinary(BINARY_OP.get(builtin), inputs);
            case SHL, ASHR, LSHR -> evaluateShift(SHIFT_OP.get(builtin), inputs);
            case OR, AND, XOR, NAND, NOR -> evaluateMultiIn(BINARY_OP.get(builtin), inputs);
            case EQ, NE, SLT, SGT, SLE, ULT, SGE, UGT, ULE, UGE -> evaluateRelation(RELATIONAL_OP.get(builtin), inputs);
            case EXT -> throw new TodoException(); // TODO:
            case REGISTER, FROM, IGNORE, SPLIT, TO -> throw new TodoException("Replace this exception");
        };
    }

    private static List<BitVector> evaluateMultiIn(BinaryOperator<BitVector> op, List<BitVector> inputs) {
        return List.of(inputs.stream().reduce(op).orElseThrow());
    }

    private static List<BitVector> evaluateBinary(BinaryOperator<BitVector> op, List<BitVector> inputs) {
        if (inputs.size() != 2) {
            throw new IllegalArgumentException("Binary operation must have exactly two inputs");
        }
        return List.of(op.apply(inputs.get(0), inputs.get(1)));
    }

    private static List<BitVector> evaluateShift(
            BiFunction<BitVector, Integer, BitVector> op, List<BitVector> inputs) {
        if (inputs.size() != 2) {
            throw new IllegalArgumentException("Shift operation must have exactly two inputs");
        }
        return List.of(op.apply(inputs.get(0), inputs.get(1).toInt()));
    }

    private static List<BitVector> evaluateRelation(
            BiFunction<BitVector, BitVector, Boolean> op, List<BitVector> inputs) {
        if (inputs.size() != 2) {
            throw new IllegalArgumentException("Binary operation must have exactly two inputs");
        }
        return List.of(BitVector.fromBoolean(op.apply(inputs.get(0), inputs.get(1))));
    }

    private static List<BitVector> evaluateNotGate(List<BitVector> inputs) {
        if (inputs.size() != 1) {
            throw new IllegalArgumentException("NOT gate must have exactly one input");
        }
        return List.of(inputs.getFirst().not());
    }
}
