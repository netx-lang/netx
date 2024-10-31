package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.PrimarySymbol;

import java.util.Objects;

public final class BindingNode extends ElementNode {
    private PrimarySymbol head;
    private ExprNode expr;

    private BindingNode(PrimarySymbol head, ExprNode expr) {
        this.head = head;
        this.expr = expr;
    }

    public static BindingNode of(PrimarySymbol head, ExprNode expr) {
        return new BindingNode(head, expr);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        expr.walk(listener);
        listener.exit(this);
    }

    public PrimarySymbol getHead() {
        return head;
    }

    public void setHead(PrimarySymbol head) {
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
        var that = (BindingNode) obj;
        return Objects.equals(this.head, that.head) && Objects.equals(this.expr, that.expr);
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
