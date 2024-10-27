package pascal.hardscript.compiler.model.instantiation;

import java.util.List;

public record ParallelType(List<InstanceType> types) implements InstanceType {

    public static ParallelType from(List<InstanceType> types) {
        return new ParallelType(types);
    }
}
