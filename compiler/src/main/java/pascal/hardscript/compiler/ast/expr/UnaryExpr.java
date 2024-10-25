package pascal.hardscript.compiler.ast.expr;

public record UnaryExpr(Operator op, ExprNode operand) implements ExprNode {

    public static UnaryExpr of(Operator op, ExprNode operand) {
        return new UnaryExpr(op, operand);
    }

    public enum Operator {
        PLUS, MINUS, NOT, BITWISE_NOT
    }
}
