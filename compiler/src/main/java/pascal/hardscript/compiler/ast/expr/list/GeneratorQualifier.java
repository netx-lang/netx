package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

public record GeneratorQualifier(SymbolExpr head, ExprNode from) implements Qualifier {

    public static GeneratorQualifier of(SymbolExpr head, ExprNode from) {
        return new GeneratorQualifier(head, from);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        from.walk(listener);
        listener.exit(this);
    }
}
