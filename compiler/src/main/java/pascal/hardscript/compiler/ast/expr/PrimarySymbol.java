package pascal.hardscript.compiler.ast.expr;

public abstract sealed class PrimarySymbol extends SymbolExpr permits IdentifierSymbol, InterpolatedSymbol {
}
