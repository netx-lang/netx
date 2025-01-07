package pascal.netx.compiler.model.variable;

import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.BitVector;

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

    @Override
    public Type getType() {
        return BitType.forBoolean();
    }

    @Override
    public BitVector toBitVector() {
        return BitVector.fromBoolean(value);
    }
}