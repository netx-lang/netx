package pascal.hardscript.compiler.model.connection.instantiation;

public record CombineType(InstanceType lhs, InstanceType rhs) implements InstanceType {

    public static CombineType from(InstanceType lhs, InstanceType rhs) {
        return new CombineType(lhs, rhs);
    }
}
