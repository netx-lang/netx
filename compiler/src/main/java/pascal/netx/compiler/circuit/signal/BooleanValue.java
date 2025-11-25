package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;

public record BooleanValue(boolean value) implements Value {

    public static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }

    public BooleanValue not() {
        return BooleanValue.of(!value);
    }

    public BooleanValue and(BooleanValue other) {
        return BooleanValue.of(value && other.value);
    }

    public BooleanValue or(BooleanValue other) {
        return BooleanValue.of(value || other.value);
    }

    public BooleanValue eq(BooleanValue other) {
        return BooleanValue.of(value == other.value);
    }

    @Nonnull
    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @Override
    public Type getType() {
        return BitType.forBoolean();
    }

    @Override
    public BitVector toBitVector() {
        return BitVector.fromBoolean(value);
    }
}