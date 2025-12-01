package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;

public record SequentialExpr(Expr lhs, Expr rhs) implements Expr {

    public static SequentialExpr of(Expr lhs, Expr rhs) {
        return new SequentialExpr(lhs, rhs);
    }

    @Override
    @Nonnull
    public String toString() {
        return lhs + " <> " + rhs;
    }

    @Override
    public SourceLoc getSourceLoc() {
        return lhs.getSourceLoc();
    }
}
