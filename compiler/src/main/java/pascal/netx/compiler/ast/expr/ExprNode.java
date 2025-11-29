package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits AccessExpr, BinaryExpr, ConditionalExpr, FieldExpr, LetExpr, ListExpr, NumberExpr, NamedPortExpr, StringExpr, SymbolExpr, UnaryExpr {
}
