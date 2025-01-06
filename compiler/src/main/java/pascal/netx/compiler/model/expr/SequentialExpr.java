package pascal.netx.compiler.model.expr;

public record SequentialExpr(boolean isTied, Expr lhs, Expr rhs) implements Expr {

    public static SequentialExpr ofSplit(Expr lhs, Expr rhs) {
        return new SequentialExpr(false, lhs, rhs);
    }

    public static SequentialExpr ofTied(Expr lhs, Expr rhs) {
        return new SequentialExpr(true, lhs, rhs);
    }
}
