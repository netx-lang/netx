package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.util.collection.BiMap;

import java.util.Objects;

import static pascal.netx.compiler.ast.expr.UnaryExpr.Operator.*;

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
        if (op == SOURCE) {
            return operand + "#";
        } else if (op == SINK) {
            return "#" + operand;
        } else {
            return OP_MAP.getKey(op) + operand;
        }
    }

    public enum Operator {
        ADD, SUB, NOT, BITWISE_NOT, SINK, SOURCE;

        public static final BiMap<String, Operator> OP_MAP = new BiMap<>();

        static {
            OP_MAP.put("+", ADD);
            OP_MAP.put("-", SUB);
            OP_MAP.put("!", NOT);
            OP_MAP.put("~", BITWISE_NOT);
        }

        public static Operator fromString(String s) {
            return OP_MAP.getValue(s);
        }
    }
}
