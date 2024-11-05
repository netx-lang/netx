package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.Map;
import java.util.Objects;

public final class BinaryExpr extends ExprNode {
    private Operator op;
    private ExprNode lhs;
    private ExprNode rhs;

    private BinaryExpr(Operator op, ExprNode lhs, ExprNode rhs) {
        this.op = op;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public static BinaryExpr of(Operator op, ExprNode lhs, ExprNode rhs) {
        return new BinaryExpr(op, lhs, rhs);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        lhs.walk(listener);
        rhs.walk(listener);
        listener.exit(this);
    }

    public Operator getOp() {
        return op;
    }

    public void setOp(Operator op) {
        this.op = op;
    }

    public ExprNode getLhs() {
        return lhs;
    }

    public void setLhs(ExprNode lhs) {
        this.lhs = lhs;
    }

    public ExprNode getRhs() {
        return rhs;
    }

    public void setRhs(ExprNode rhs) {
        this.rhs = rhs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BinaryExpr) obj;
        return Objects.equals(this.op, that.op) &&
                Objects.equals(this.lhs, that.lhs) &&
                Objects.equals(this.rhs, that.rhs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(op, lhs, rhs);
    }

    private static final Map<Operator, String> OPERATOR_MAP = Map.ofEntries(
            Map.entry(Operator.COMBINE, "<>"),
            Map.entry(Operator.CONCAT, "::"),
            Map.entry(Operator.POW, "**"), Map.entry(Operator.MUL, "*"),
            Map.entry(Operator.DIV, "/"), Map.entry(Operator.MOD, "%"),
            Map.entry(Operator.ADD, "+"), Map.entry(Operator.SUB, "-"),
            Map.entry(Operator.ARITH_SHL, "<<<"), Map.entry(Operator.ARITH_SHR, ">>>"),
            Map.entry(Operator.LOGIC_SHR, ">>"), Map.entry(Operator.LOGIC_SHL, "<<"),
            Map.entry(Operator.EQ, "=="), Map.entry(Operator.NE, "!="),
            Map.entry(Operator.LT, "<"), Map.entry(Operator.LE, "<="),
            Map.entry(Operator.GT, ">"), Map.entry(Operator.GE, ">="),
            Map.entry(Operator.AND, "&"), Map.entry(Operator.OR, "|"),
            Map.entry(Operator.XOR, "^"), Map.entry(Operator.XNOR, "^^"),
            Map.entry(Operator.LOGIC_AND, "&&"), Map.entry(Operator.LOGIC_OR, "||")
    );

    @Override
    public String toString() {
        return "(" + lhs + " " + OPERATOR_MAP.get(op) + " " + rhs + ")";
    }

    public enum Operator {
        COMBINE, CONCAT,
        POW, MUL, DIV, MOD, ADD, SUB,
        ARITH_SHL, ARITH_SHR, LOGIC_SHR, LOGIC_SHL,
        EQ, NE, LT, LE, GT, GE,
        AND, OR, XOR, XNOR, LOGIC_AND, LOGIC_OR,
    }
}
