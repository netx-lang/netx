package pascal.hardscript.compiler.model.component;

public record LambdaType(ComponentType value) implements ComponentType {
    public static LambdaType of(ComponentType value) {
        return new LambdaType(value);
    }
}
