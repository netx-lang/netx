package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.util.collection.BiMap;

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

    @Override
    public String toString() {
        return "(" + lhs + " " + Operator.OP_MAP.getKey(op) + " " + rhs + ")";
    }

    public enum Operator {
        SEQ, PAR,
        CONCAT,
        POW, MUL, DIV, MOD, ADD, SUB,
        ARITH_SHL, ARITH_SHR, LOGIC_SHR, LOGIC_SHL,
        EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
        AND, OR, XOR, XNOR, LOGIC_AND, LOGIC_OR;

        public static final BiMap<String, Operator> OP_MAP = new BiMap<>();

        static {
            OP_MAP.put("<>", SEQ);
            OP_MAP.put("||", PAR);
            OP_MAP.put("::", CONCAT);
            OP_MAP.put("**", POW);
            OP_MAP.put("+", ADD);
            OP_MAP.put("-", SUB);
            OP_MAP.put("*", MUL);
            OP_MAP.put("/", DIV);
            OP_MAP.put("%", MOD);
            OP_MAP.put("==", EQ);
            OP_MAP.put("!=", NE);
            OP_MAP.put("<*", ULT);
            OP_MAP.put(">*", UGT);
            OP_MAP.put("<=*", ULE);
            OP_MAP.put(">=*", UGE);
            OP_MAP.put("<", SLT);
            OP_MAP.put(">", SGT);
            OP_MAP.put("<=", SLE);
            OP_MAP.put(">=", SGE);
            OP_MAP.put("and", LOGIC_AND);
            OP_MAP.put("or", LOGIC_OR);
            OP_MAP.put("&", AND);
            OP_MAP.put("|", OR);
            OP_MAP.put("^", XOR);
            OP_MAP.put("^~", XNOR);
            OP_MAP.put("~^", XNOR);
            OP_MAP.put("<<", LOGIC_SHL);
            OP_MAP.put(">>", LOGIC_SHR);
            OP_MAP.put("<<<", ARITH_SHL);
            OP_MAP.put(">>>", ARITH_SHR);
        }

        public static Operator fromString(String s) {
            return Objects.requireNonNull(OP_MAP.getValue(s));
        }
    }
}
