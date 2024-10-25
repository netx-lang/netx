package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

public record UnaryExpr(Operator op, ExprNode operand) implements ExprNode {

    public static UnaryExpr of(Operator op, ExprNode operand) {
        return new UnaryExpr(op, operand);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        operand.walk(listener);
        listener.exit(this);
    }

    public enum Operator {
        PLUS, MINUS, NOT, BITWISE_NOT
    }
}
