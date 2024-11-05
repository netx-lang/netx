package pascal.hardscript.compiler.model.data;

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
    public DataType getType() {
        return BitType.forBoolean();
    }
}