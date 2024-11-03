package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.selector.Selector;

import java.util.Objects;

public final class AccessExpr extends ExprNode {
    private ExprNode base;
    private Selector selector;

    private AccessExpr(ExprNode base, Selector selector) {
        this.base = base;
        this.selector = selector;
    }

    public static AccessExpr of(ExprNode base, Selector selector) {
        return new AccessExpr(base, selector);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        base.walk(listener);
        selector.walk(listener);
        listener.exit(this);
    }

    public ExprNode getBase() {
        return base;
    }

    public void setBase(ExprNode base) {
        this.base = base;
    }

    public Selector getSelector() {
        return selector;
    }

    public void setSelector(Selector selector) {
        this.selector = selector;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (AccessExpr) obj;
        return Objects.equals(this.base, that.base) &&
                Objects.equals(this.selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, selector);
    }

    @Override
    public String toString() {
        return base.toString() + selector.toString();
    }
}
