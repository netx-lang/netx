package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.List;

public record ParallelExpr(List<Expr> exprs) implements Expr {

    public static ParallelExpr from(List<Expr> exprs) {
        return new ParallelExpr(exprs);
    }

    @Override
    public SourceLoc getSourceLoc() {
        if (exprs.isEmpty()) {
            return SourceLoc.none();
        }
        return exprs.getFirst().getSourceLoc();
    }

    @Override
    @Nonnull
    public String toString() {
        return "[" + String.join(", ", exprs.stream().map(Expr::toString).toList()) + "]";
    }
}
