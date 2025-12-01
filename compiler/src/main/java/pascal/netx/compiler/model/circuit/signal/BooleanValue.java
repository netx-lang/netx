package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.model.ast.expr.BinaryExpr;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
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
            default -> throw new ShallNotReachHereError();
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
}