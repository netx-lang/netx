package pascal.netx.compiler.model.ast.expr;

import pascal.netx.compiler.model.ast.ASTListener;

public final class StringExpr extends ExprNode {
    private final String value;

    private StringExpr(String value) {
        this.value = value;
    }

    public static StringExpr of(String value) {
        return new StringExpr(value);
    }

    public String getValue() {
        return value;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
