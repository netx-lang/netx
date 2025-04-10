package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.Entity;

public sealed interface Expr extends Entity permits DecoratedExpr, InstantiationRef, ParallelExpr, PortConnectionExpr, PrimaryExpr, SequentialExpr {
}
