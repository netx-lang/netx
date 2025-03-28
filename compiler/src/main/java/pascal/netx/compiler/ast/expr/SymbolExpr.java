package pascal.netx.compiler.ast.expr;

public abstract sealed class SymbolExpr extends ExprNode permits IdentifierExpr, CallSymbolExpr {

}
