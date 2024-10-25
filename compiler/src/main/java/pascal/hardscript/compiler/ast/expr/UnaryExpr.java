package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.Objects;

public final class UnaryExpr extends ExprNode {
    private Operator op;
    private ExprNode operand;

    private UnaryExpr(Operator op, ExprNode operand) {
        this.op = op;
        this.operand = operand;
    }

    public static UnaryExpr of(Operator op, ExprNode operand) {
        return new UnaryExpr(op, operand);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        operand.walk(listener);
        listener.exit(this);
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    public ExprNode getOperand() {
        return operand;
    }

    public void setOperand(ExprNode operand) {
        this.operand = operand;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (UnaryExpr) obj;
        return Objects.equals(this.op, that.op) &&
                Objects.equals(this.operand, that.operand);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, operand);
    }

    @Override
    public String toString() {
        return "UnaryExpr[" +
                "op=" + op + ", " +
                "operand=" + operand + ']';
    }

    public enum Operator {
        PLUS, MINUS, NOT, BITWISE_NOT
    }
}
