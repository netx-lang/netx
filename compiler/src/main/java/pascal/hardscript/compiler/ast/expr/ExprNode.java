package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTNode;

public abstract sealed class ExprNode extends ASTNode permits
        NumberExpr, UnaryExpr, ParamSymbolExpr, BinaryExpr, ConditionalExpr, ListExpr, AccessExpr, SymbolExpr {
}
