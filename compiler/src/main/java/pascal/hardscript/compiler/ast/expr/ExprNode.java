package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits AccessExpr, AutoTypeExpr, BinaryExpr, ConditionalExpr, MacroExpr, ListExpr, NumberExpr, SymbolExpr, UnaryExpr {
}
