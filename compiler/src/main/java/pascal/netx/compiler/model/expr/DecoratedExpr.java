package pascal.netx.compiler.model.expr;

public record DecoratedExpr(Decorator decorator, Expr operand) implements Expr {

    public static DecoratedExpr of(Decorator decorator, Expr operand) {
        return new DecoratedExpr(decorator, operand);
    }

    @Override
    public String toString() {
        return switch (decorator) {
            case BLOCK_IN -> operand + "#";
            case BLOCK_OUT -> "#" + operand;
            case NON_BLOCK -> operand.toString();
        };
    }

    public enum Decorator {
        BLOCK_IN, BLOCK_OUT, NON_BLOCK
    }
}
