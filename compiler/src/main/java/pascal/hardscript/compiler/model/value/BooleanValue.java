package pascal.hardscript.compiler.model.value;

public record BooleanValue(boolean value) implements Value {

    public BooleanValue not() {
        return BooleanValue.of(!value);
    }

    public static BooleanValue of(boolean value) {
        return new BooleanValue(value);
    }
}