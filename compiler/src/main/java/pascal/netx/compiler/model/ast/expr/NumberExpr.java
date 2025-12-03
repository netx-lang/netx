package pascal.netx.compiler.model.ast.expr;

public abstract sealed class NumberExpr extends ExprNode permits BitVectorNumber, BoolNumber, FloatNumber, IntNumber {
}
