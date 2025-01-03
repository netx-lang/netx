package pascal.netx.compiler.model.component;

import pascal.netx.compiler.model.Entity;

public sealed interface ComponentType extends Entity permits AtomType, ParallelType, SequentialType {
}
