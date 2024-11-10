package pascal.hardscript.compiler.model.component;

public record SequentialType(ComponentType lhs, ComponentType rhs) implements ComponentType {

    public static SequentialType of(ComponentType lhs, ComponentType rhs) {
        return new SequentialType(lhs, rhs);
    }
}
