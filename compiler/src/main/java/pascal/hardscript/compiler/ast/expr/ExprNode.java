package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits
        NumberExpr, UnaryExpr, BinaryExpr, ConditionalExpr, ListExpr, AccessExpr, SymbolExpr {
}
