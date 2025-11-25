package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.Entity;

public sealed interface Expr extends Entity permits DecoratedExpr, InstantiationRef, ParallelExpr, PortExpr, PrimaryExpr, SequentialExpr {
}
