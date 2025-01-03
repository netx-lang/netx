package pascal.netx.compiler.model.component;

import java.util.List;

public record ParallelType(List<ComponentType> types) implements ComponentType {

    public static ParallelType from(List<ComponentType> types) {
        return new ParallelType(types);
    }
}
