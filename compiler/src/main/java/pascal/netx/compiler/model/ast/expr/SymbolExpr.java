package pascal.netx.compiler.model.ast.expr;

import pascal.netx.util.lang.Identifier;

public abstract sealed class SymbolExpr extends ExprNode permits IdentifierExpr, CallExpr {
    public abstract Identifier getSymbol();
}
