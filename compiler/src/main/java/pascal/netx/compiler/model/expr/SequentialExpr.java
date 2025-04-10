package pascal.netx.compiler.model.expr;

public record SequentialExpr(Expr lhs, Expr rhs) implements Expr {

    public static SequentialExpr of(Expr lhs, Expr rhs) {
        return new SequentialExpr(lhs, rhs);
    }

    @Override
    public String toString() {
        return lhs + " <> " + rhs;
    }
}
