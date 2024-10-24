package pascal.hardscript.compiler.ast.expr.atom;

import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record SymbolExpr(Symbol id) implements AtomExpr, ExprNode {

    public static SymbolExpr of(Symbol id) {
        return new SymbolExpr(id);
    }
}
