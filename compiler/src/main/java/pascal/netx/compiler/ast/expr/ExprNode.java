package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits AccessExpr, BinaryExpr, ConditionalExpr, FieldExpr, ListExpr, NumberExpr, NamedSequentialExpr, SymbolExpr, UnaryExpr {
}
