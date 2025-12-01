package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.util.lang.SourceLoc;

public sealed interface Expr extends Entity permits DecoratedExpr, InstantiationRef, ParallelExpr, NamedPortExpr, PrimaryExpr, SequentialExpr {
    SourceLoc getSourceLoc();
}
