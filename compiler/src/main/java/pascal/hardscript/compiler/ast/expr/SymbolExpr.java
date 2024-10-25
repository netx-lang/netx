package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Symbol;

public record SymbolExpr(Symbol symbol) implements ExprNode {

    public static SymbolExpr of(Symbol symbol) {
        return new SymbolExpr(symbol);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }
}
