package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits AccessExpr, BinaryExpr, ConditionalExpr, ListExpr, NumberExpr, SymbolExpr, UnaryExpr {
}
