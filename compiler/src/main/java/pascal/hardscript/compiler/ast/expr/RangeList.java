package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.Objects;

public final class RangeList extends ListExpr {
    private ExprNode from;
    private ExprNode to;

    private RangeList(ExprNode from, ExprNode to) {
        this.from = from;
        this.to = to;
    }

    public static RangeList of(ExprNode from, ExprNode to) {
        return new RangeList(from, to);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        from.walk(listener);
        to.walk(listener);
        listener.exit(this);
    }

    public ExprNode getFrom() {
        return from;
    }

    public void setFrom(ExprNode from) {
        this.from = from;
    }

    public ExprNode getTo() {
        return to;
    }

    public void setTo(ExprNode to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (RangeList) obj;
        return Objects.equals(this.from, that.from) &&
                Objects.equals(this.to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return "[" + from + ".." + to + "]";
    }
}
