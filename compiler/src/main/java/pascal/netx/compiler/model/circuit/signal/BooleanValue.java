package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.ast.expr.BinaryExpr;
import pascal.netx.compiler.model.ast.expr.UnaryExpr;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;
import java.util.Objects;

public record BooleanValue(boolean value) implements Value {

    public static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    @Nonnull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static BinaryOp dispatchBinaryOp(BinaryExpr.Operator op) {
        return switch (op) {
            case LOGIC_AND -> (a, b) -> BooleanValue.of(a.value && b.value);
            case LOGIC_OR -> (a, b) -> BooleanValue.of(a.value || b.value);
            default -> {
                Logger.error("Binary operator " + op + " is not supported for boolean values.");
                throw new CompileCancellationException();
            }
        };
    }

    public static UnaryOp dispatchUnaryOp(UnaryExpr.Operator op) {
        return switch (op) {
            case NOT, BITWISE_NOT -> v -> BooleanValue.of(!v.value);
            default -> {
                Logger.error("Unary operator " + op + " is not supported for boolean values.");
                throw new CompileCancellationException();
            }
        };
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BooleanValue that = (BooleanValue) o;
        return value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value);
    }

    @Override
    public BitVector toBitVector() {
        return BitVector.fromBoolean(value);
    }

    @Override
    public Type getType() {
        return BitType.forBoolean();
    }

    public interface BinaryOp {
        BooleanValue apply(BooleanValue lhs, BooleanValue rhs);
    }

    public interface UnaryOp {
        BooleanValue apply(BooleanValue v);
    }
}