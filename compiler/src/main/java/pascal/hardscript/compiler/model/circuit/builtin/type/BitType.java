package pascal.hardscript.compiler.model.circuit.builtin.type;

public record BitType(int width) implements ComponentType {

    public static BitType of(int width) {
        return new BitType(width);
    }
}
