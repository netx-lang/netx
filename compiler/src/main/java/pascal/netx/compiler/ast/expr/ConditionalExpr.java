package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;

import java.util.Objects;

public final class ConditionalExpr extends ExprNode {
    private ExprNode condition;
    private ExprNode thenBranch;
    private ExprNode elseBranch;

    private ConditionalExpr(ExprNode condition, ExprNode thenBranch, ExprNode elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

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

    public ExprNode getCondition() {
        return condition;
    }

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    public ExprNode getThenBranch() {
        return thenBranch;
    }

    public void setThenBranch(ExprNode thenBranch) {
        this.thenBranch = thenBranch;
    }

    public ExprNode getElseBranch() {
        return elseBranch;
    }

    public void setElseBranch(ExprNode elseBranch) {
        this.elseBranch = elseBranch;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ConditionalExpr) obj;
        return Objects.equals(this.condition, that.condition) &&
                Objects.equals(this.thenBranch, that.thenBranch) &&
                Objects.equals(this.elseBranch, that.elseBranch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, thenBranch, elseBranch);
    }

    @Override
    public String toString() {
        return "if " + condition + " then " + thenBranch + " else " + elseBranch;
    }
}
