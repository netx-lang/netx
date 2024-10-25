package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

import java.util.List;

public record ComponentNode(List<SymbolExpr> ids, ExprNode type) implements ElementNode {

    public static ComponentNode of(List<SymbolExpr> ids, ExprNode type) {
        return new ComponentNode(ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        ids.forEach(id -> id.walk(listener));
        type.walk(listener);
        listener.exit(this);
    }
}
