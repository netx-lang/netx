package pascal.netx.compiler.model.component;

import java.util.List;

public record ParallelExpr(List<Expr> exprs) implements Expr {

    public static ParallelExpr from(List<Expr> exprs) {
        return new ParallelExpr(exprs);
    }
}
