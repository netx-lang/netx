package pascal.hardscript.compiler.ast.expr;

public abstract sealed class ListExpr extends ExprNode permits
        RangeList, ListComprehension, CommonList {
}
