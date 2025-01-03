package pascal.netx.compiler.ast.expr.selector;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class PartSelector extends Selector {
    private ExprNode start;
    private ExprNode end;

    private PartSelector(ExprNode start, ExprNode end) {
        this.start = start;
        this.end = end;
    }

    public static PartSelector of(ExprNode start, ExprNode end) {
        return new PartSelector(start, end);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        start.walk(listener);
        end.walk(listener);
        listener.exit(this);
    }

    public ExprNode getStart() {
        return start;
    }

    public void setStart(ExprNode start) {
        this.start = start;
    }

    public ExprNode getEnd() {
        return end;
    }

    public void setEnd(ExprNode end) {
        this.end = end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (PartSelector) obj;
        return Objects.equals(this.start, that.start) &&
                Objects.equals(this.end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return String.format("[%s:%s]", start, end);
    }
}
