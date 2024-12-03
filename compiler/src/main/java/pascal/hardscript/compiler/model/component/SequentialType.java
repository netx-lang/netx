package pascal.hardscript.compiler.model.component;

public record SequentialType(boolean tied, ComponentType lhs, ComponentType rhs) implements ComponentType {

    public static SequentialType ofSplit(ComponentType lhs, ComponentType rhs) {
        return new SequentialType(false, lhs, rhs);
    }

    public static SequentialType ofTied(ComponentType lhs, ComponentType rhs) {
        return new SequentialType(true, lhs, rhs);
    }
}
