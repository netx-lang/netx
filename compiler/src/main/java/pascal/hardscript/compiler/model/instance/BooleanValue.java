package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

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
    public InstanceType getType() {
        return BitType.forBoolean();
    }
}