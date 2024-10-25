package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.Symbol;

public record SymbolExpr(Symbol id) implements ExprNode {

    public static SymbolExpr of(Symbol id) {
        return new SymbolExpr(id);
    }
}
