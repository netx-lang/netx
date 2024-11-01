package pascal.hardscript.compiler.model.data;

public record BitType(int width) implements DataType {

    public static BitType of(int width) {
        return new BitType(width);
    }
}
