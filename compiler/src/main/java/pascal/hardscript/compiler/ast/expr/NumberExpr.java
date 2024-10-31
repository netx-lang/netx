package pascal.hardscript.compiler.ast.expr;

public abstract sealed class NumberExpr extends ExprNode permits BitVectorNumber, BoolNumber, IntNumber {
}
