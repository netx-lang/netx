package pascal.hardscript.compiler.ast.expr;

public record BinaryExpr(Operator op, ExprNode lhs, ExprNode rhs) implements ExprNode {

    public static BinaryExpr of(Operator op, ExprNode lhs, ExprNode rhs) {
        return new BinaryExpr(op, lhs, rhs);
    }

    public enum Operator {
        COMBINE, MAPS_TO,
        POW, MUL, DIV, MOD, ADD, SUB,
        ARITH_SHL, ARITH_SHR, LOGIC_SHR, LOGIC_SHL,
        EQ, NE, LT, LE, GT, GE,
        AND, OR, XOR, XNOR, LOGIC_AND, LOGIC_OR,
    }
}
