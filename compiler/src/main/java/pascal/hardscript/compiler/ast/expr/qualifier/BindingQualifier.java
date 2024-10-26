package pascal.hardscript.compiler.ast.expr.qualifier;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class BindingQualifier extends Qualifier {
    private Symbol head;
    private ExprNode expr;

    private BindingQualifier(Symbol head, ExprNode expr) {
        this.head = head;
        this.expr = expr;
    }

    public static BindingQualifier of(Symbol head, ExprNode expr) {
        return new BindingQualifier(head, expr);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        expr.walk(listener);
        listener.exit(this);
    }

    public Symbol getHead() {
        return head;
    }

    public void setHead(Symbol head) {
        this.head = head;
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BindingQualifier) obj;
        return Objects.equals(this.head, that.head) &&
                Objects.equals(this.expr, that.expr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, expr);
    }

    @Override
    public String toString() {
        return "BindingQualifier[" +
                "head=" + head + ", " +
                "expr=" + expr + ']';
    }
}
