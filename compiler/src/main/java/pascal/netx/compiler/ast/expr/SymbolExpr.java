package pascal.netx.compiler.ast.expr;

import pascal.netx.util.lang.Identifier;

public abstract sealed class SymbolExpr extends ExprNode permits IdentifierExpr, CallExpr {
    public abstract Identifier getSymbol();
}
