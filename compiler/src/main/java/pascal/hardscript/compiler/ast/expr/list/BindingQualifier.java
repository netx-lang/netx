package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

public record BindingQualifier(SymbolExpr head, ExprNode expr) implements Qualifier {

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
}
