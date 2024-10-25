package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

public record ConditionalExpr(ExprNode condition, ExprNode thenBranch, ExprNode elseBranch) implements ExprNode {

    public static ConditionalExpr of(ExprNode condition, ExprNode thenBranch, ExprNode elseBranch) {
        return new ConditionalExpr(condition, thenBranch, elseBranch);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        condition.walk(listener);
        thenBranch.walk(listener);
        elseBranch.walk(listener);
        listener.exit(this);
    }
}
