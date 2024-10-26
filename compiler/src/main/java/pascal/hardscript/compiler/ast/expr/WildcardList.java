package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

public final class WildcardList extends ListExpr {
    private static final WildcardList INSTANCE = new WildcardList();

    public static WildcardList getInstance() {
        return INSTANCE;
    }

    private WildcardList() {
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
