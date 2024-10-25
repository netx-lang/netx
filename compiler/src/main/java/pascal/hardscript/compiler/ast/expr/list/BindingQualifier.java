package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

import java.util.Objects;

public final class BindingQualifier extends Qualifier {
    private SymbolExpr head;
    private ExprNode expr;

    private BindingQualifier(SymbolExpr head, ExprNode expr) {
        this.head = head;
        this.expr = expr;
    }

    public static BindingQualifier of(SymbolExpr head, ExprNode expr) {
        return new BindingQualifier(head, expr);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        expr.walk(listener);
        listener.exit(this);
    }

    public SymbolExpr getHead() {
        return head;
    }

    public void setHead(SymbolExpr head) {
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
