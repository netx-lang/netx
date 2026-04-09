package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.model.ast.expr.BinaryExpr;
import pascal.netx.compiler.model.ast.expr.UnaryExpr;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;
import java.util.Objects;

import static pascal.netx.compiler.model.ast.expr.BinaryExpr.Operator.*;

public record VectorValue(BitVector value) implements Value {
    public static VectorValue of(BitVector value) {
        return new VectorValue(value);
    }

    public static VectorValue fromInt(int value) {
        return VectorValue.of(BitVector.fromInt(value));
    }

    public static BinaryOp dispatchBinaryOp(BinaryExpr.Operator op) {
        return switch (op) {
            case ADD, SUB, MUL, DIV, MOD, POW, OR, AND, XOR, XNOR ->
                    (a, b) -> VectorValue.of(ARITHMETIC_OPS.get(op).apply(a.value, b.value));
            case EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE ->
                    (a, b) -> BooleanValue.of(RELATIONAL_OPS.get(op).apply(a.value, b.value));
            case ARITH_SHR, LOGIC_SHR, ARITH_SHL, LOGIC_SHL ->
                    (a, b) -> VectorValue.of(SHIFT_OPS.get(op).apply(a.value, b.toInt()));
            default -> throw new ShallNotReachHereError();
        };
    }

    public static UnaryOp dispatchUnaryOp(UnaryExpr.Operator op) {
        return switch (op) {
            case POS -> identity -> identity;
            case NEG -> v -> VectorValue.of(v.value.neg());
            case NOT, BITWISE_NOT -> v -> VectorValue.of(v.value.not());
            default -> throw new ShallNotReachHereError();
        };
    }

    @Nonnull
    public String toString() {
        if (value.size() == 32) {
            return String.valueOf(value.toULong());
        }
        return value.toString();
    }

    public int toInt() {
        return value.toUInt();
    }

    public boolean isZero() {
        return value.isZero();
    }

    public BitVector toBitVector() {
        return value.copyOf();
    }

    public Type getType() {
        return BitType.of(value.size());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        VectorValue that = (VectorValue) o;
        return Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    public interface BinaryOp {
        Value apply(VectorValue a, VectorValue b);
    }

    public interface UnaryOp {
        VectorValue apply(VectorValue v);
    }
}