package pascal.hardscript.compiler.model.connection;

import pascal.hardscript.compiler.model.Entity;

public sealed interface InstanceType extends Entity permits AtomType, CombineType, ParallelType {
}
