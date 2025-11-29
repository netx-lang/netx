package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.util.lang.SourceLoc;

public sealed interface Expr extends Entity permits DecoratedExpr, InstantiationRef, ParallelExpr, NamedPortExpr, PrimaryExpr, SequentialExpr {
    SourceLoc getSourceLoc();
}
