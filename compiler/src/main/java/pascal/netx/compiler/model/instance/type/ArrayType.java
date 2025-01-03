package pascal.netx.compiler.model.instance.type;

import java.util.List;

public record ArrayType(InstanceType baseType, int size) implements InstanceType {
    public static ArrayType of(InstanceType baseType, int size) {
        return new ArrayType(baseType, size);
    }

    public static ArrayType of(InstanceType baseType, List<Integer> sizes) {
        var result = baseType;
        for (var size : sizes) {
            result = new ArrayType(result, size);
        }
        return (ArrayType) result;
    }
}
