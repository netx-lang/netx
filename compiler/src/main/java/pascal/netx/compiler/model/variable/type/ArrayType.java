package pascal.netx.compiler.model.variable.type;

import java.util.List;

public record ArrayType(Type baseType, int size) implements Type {
    public static ArrayType of(Type baseType, int size) {
        return new ArrayType(baseType, size);
    }

    public static ArrayType of(Type baseType, List<Integer> sizes) {
        var result = baseType;
        for (var size : sizes) {
            result = new ArrayType(result, size);
        }
        return (ArrayType) result;
    }
}
