package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;

public sealed interface ComponentType extends Entity permits AtomType, CombineType, ParallelType {
}
