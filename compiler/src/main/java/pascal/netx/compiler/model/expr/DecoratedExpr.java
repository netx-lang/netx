package pascal.netx.compiler.model.expr;

public record DecoratedExpr(Decorator decorator, Expr operand) implements Expr {

    public static DecoratedExpr of(Decorator decorator, Expr operand) {
        return new DecoratedExpr(decorator, operand);
    }

    public enum Decorator {
        SOURCE, SINK, BIDI
    }
}
