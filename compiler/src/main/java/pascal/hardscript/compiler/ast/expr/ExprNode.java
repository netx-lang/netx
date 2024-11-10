package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits AccessExpr, AutoTypeExpr, BinaryExpr, ConditionalExpr, LambdaExpr, ListExpr, NumberExpr, SymbolExpr, UnaryExpr {
}
