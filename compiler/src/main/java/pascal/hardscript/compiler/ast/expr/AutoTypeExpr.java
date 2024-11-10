package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

public final class AutoTypeExpr extends ExprNode {
    public static AutoTypeExpr of() {
        return new AutoTypeExpr();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
