package pascal.hardscript.compiler.ast.expr.access;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record AccessExpr(ExprNode base, Selector selector) implements ExprNode {

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
}
