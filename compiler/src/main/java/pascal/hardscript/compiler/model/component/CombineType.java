package pascal.hardscript.compiler.model.component;

public record CombineType(ComponentType lhs, ComponentType rhs) implements ComponentType {

    public static CombineType from(ComponentType lhs, ComponentType rhs) {
        return new CombineType(lhs, rhs);
    }
}
