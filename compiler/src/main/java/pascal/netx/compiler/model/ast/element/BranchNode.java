package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.expr.ExprNode;

import java.util.Objects;

public final class BranchNode extends ASTNode implements ComponentElement {
    private ExprNode condition;
    private ComponentElement thenBranch, elseBranch;

    private BranchNode(ExprNode condition, ComponentElement thenBranch, ComponentElement elseBranch) {
        this.condition = condition;
        this.thenBranch = thenBranch;
        this.elseBranch = elseBranch;
    }

    public static BranchNode of(ExprNode condition, ComponentElement thenBranch, ComponentElement elseBranch) {
        return new BranchNode(condition, thenBranch, elseBranch);
    }

    public ExprNode getCondition() {
        return condition;
    }

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    public ComponentElement getThenBranch() {
        return thenBranch;
    }

    public void setThenBranch(ComponentElement thenBranch) {
        this.thenBranch = thenBranch;
    }

    public ComponentElement getElseBranch() {
        return elseBranch;
    }

    public void setElseBranch(ComponentElement elseBranch) {
        this.elseBranch = elseBranch;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        condition.walk(listener);
        thenBranch.walk(listener);
        if (elseBranch != null) {
            elseBranch.walk(listener);
        }
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BranchNode that = (BranchNode) o;
        return Objects.equals(condition, that.condition) && Objects.equals(thenBranch, that.thenBranch) && Objects.equals(elseBranch, that.elseBranch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition, thenBranch, elseBranch);
    }

    @Override
    public String toString() {
        if (elseBranch != null) {
            return String.format("if (%s) then {\n%s\n} else {\n%s\n}", condition, thenBranch, elseBranch);
        } else {
            return String.format("if (%s) then {\n%s\n}", condition, thenBranch);
        }
    }
}
