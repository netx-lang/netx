package pascal.hardscript.compiler.model.value;

public record Int32Value(int value) implements Value, IntegerValue {
    public static Int32Value of(int value) {
        return new Int32Value(value);
    }

    @Override
    public int toInt() {
        return value;
    }

    @Override
    public Int32Value negative() {
        return Int32Value.of(-value);
    }

    @Override
    public Int32Value bitwiseNot() {
        return Int32Value.of(~value);
    }
}
