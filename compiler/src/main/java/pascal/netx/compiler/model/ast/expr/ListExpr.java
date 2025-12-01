package pascal.netx.compiler.model.ast.expr;

public abstract sealed class ListExpr extends ExprNode permits
        RangeList, ListComprehension, CommonList {
}
