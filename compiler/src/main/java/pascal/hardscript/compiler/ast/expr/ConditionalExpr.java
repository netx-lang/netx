package pascal.hardscript.compiler.ast.expr;

public record ConditionalExpr(ExprNode condition, ExprNode thenBranch, ExprNode elseBranch) implements ExprNode {

    public static ConditionalExpr of(ExprNode condition, ExprNode thenBranch, ExprNode elseBranch) {
        return new ConditionalExpr(condition, thenBranch, elseBranch);
    }
}
