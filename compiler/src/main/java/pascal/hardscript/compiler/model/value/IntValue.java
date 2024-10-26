package pascal.hardscript.compiler.model.value;

public record IntValue(int value) implements Value {
    public static IntValue of(int value) {
        return new IntValue(value);
    }
}
