package pascal.netx.compiler.model.expr;

import java.util.List;

public record ParallelExpr(List<Expr> exprs) implements Expr {

    public static ParallelExpr from(List<Expr> exprs) {
        return new ParallelExpr(exprs);
    }

    @Override
    public String toString() {
        return "[" + String.join(", ", exprs.stream().map(Expr::toString).toList()) + "]";
    }
}
