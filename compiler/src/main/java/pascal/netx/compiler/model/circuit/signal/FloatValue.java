package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.expr.BinaryExpr;
import pascal.netx.compiler.model.ast.expr.UnaryExpr;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;
import java.math.BigDecimal;

public record FloatValue(int size, BigDecimal value) implements Value {
    public static FloatValue of(int size, BigDecimal value) {
        return new FloatValue(size, value);
    }

    public static FloatValue.BinaryOp dispatchBinaryOp(BinaryExpr.Operator op) {
        return switch (op) {
            case ADD -> (a, b) -> FloatValue.of(Math.max(a.size, b.size), a.value.add(b.value));
            case SUB -> (a, b) -> FloatValue.of(Math.max(a.size, b.size), a.value.subtract(b.value));
            case MUL -> (a, b) -> FloatValue.of(Math.max(a.size, b.size), a.value.multiply(b.value));
            case DIV -> (a, b) -> FloatValue.of(Math.max(a.size, b.size),
                    BigDecimal.valueOf(a.value.doubleValue() / b.value.doubleValue()));
            case POW -> (a, b) -> FloatValue.of(Math.max(a.size, b.size),
                    BigDecimal.valueOf(Math.pow(a.value.doubleValue(), b.value.doubleValue())));
            case EQ -> (a, b) -> BooleanValue.of(a.value.doubleValue() == b.value.doubleValue());
            case NE -> (a, b) -> BooleanValue.of(a.value.doubleValue() != b.value.doubleValue());
            case SLT -> (a, b) -> BooleanValue.of(a.value.doubleValue() < b.value.doubleValue());
            case SLE -> (a, b) -> BooleanValue.of(a.value.doubleValue() <= b.value.doubleValue());
            case SGT -> (a, b) -> BooleanValue.of(a.value.doubleValue() > b.value.doubleValue());
            case SGE -> (a, b) -> BooleanValue.of(a.value.doubleValue() >= b.value.doubleValue());
            case MOD -> {
                Logger.error("Modulo operation is not supported for floating point values.");
                throw new CompileCancellationException();
            }
            case OR, AND, XOR, XNOR -> {
                Logger.error("Bitwise operations are not supported for floating point values.");
                throw new CompileCancellationException();
            }
            case ULT, ULE, UGT, UGE -> {
                Logger.error("Unsigned comparisons are not supported for floating point values.");
                throw new CompileCancellationException();
            }
            case ARITH_SHR, LOGIC_SHR, ARITH_SHL, LOGIC_SHL -> {
                Logger.error("Shift operations are not supported for floating point values.");
                throw new CompileCancellationException();
            }
            default -> throw new ShallNotReachHereError();
        };
    }

    public static FloatValue.UnaryOp dispatchUnaryOp(UnaryExpr.Operator op) {
        return switch (op) {
            case POS -> identity -> identity;
            case NEG -> v -> FloatValue.of(v.size, v.value.negate());
            case NOT, BITWISE_NOT -> {
                Logger.error("Bitwise NOT operation is not supported for floating point values.");
                throw new CompileCancellationException();
            }
            default -> throw new ShallNotReachHereError();
        };
    }

    @Override
    public BitVector toBitVector() {
        switch (size) {
            case 16 -> {
                short bits = Float.floatToFloat16(value.floatValue());
                return BitVector.fromInt(16, bits & 0xFFFF);
            }
            case 32 -> {
                int bits = Float.floatToIntBits(value.floatValue());
                return BitVector.fromInt(32, bits);
            }
            case 64 -> {
                long bits = Double.doubleToLongBits(value.doubleValue());
                return BitVector.fromLong(64, bits);
            }
            default -> throw new UnsupportedOperationException("Unsupported floating size: " + size);
        }
    }

    @Override
    public Type getType() {
        return BitType.of(size);
    }

    @Override
    @Nonnull
    public String toString() {
        return VectorValue.of(toBitVector()).toString();
    }

    public interface BinaryOp {
        Value apply(FloatValue a, FloatValue b);
    }

    public interface UnaryOp {
        FloatValue apply(FloatValue v);
    }
}
