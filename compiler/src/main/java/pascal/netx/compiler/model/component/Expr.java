package pascal.netx.compiler.model.component;

import pascal.netx.compiler.model.Entity;

public sealed interface Expr extends Entity permits AtomExpr, ParallelExpr, SequentialExpr {
}
