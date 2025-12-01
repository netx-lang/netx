package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;

public record DecoratedExpr(Decorator decorator, Expr operand) implements Expr {

    public static DecoratedExpr of(Decorator decorator, Expr operand) {
        return new DecoratedExpr(decorator, operand);
    }

    @Override
    @Nonnull
    public String toString() {
        return switch (decorator) {
            case BLOCK_IN -> operand + "#";
            case BLOCK_OUT -> "#" + operand;
            case NON_BLOCK -> operand.toString();
        };
    }

    @Override
    public SourceLoc getSourceLoc() {
        return operand.getSourceLoc();
    }

    public enum Decorator {
        BLOCK_IN, BLOCK_OUT, NON_BLOCK
    }
}
