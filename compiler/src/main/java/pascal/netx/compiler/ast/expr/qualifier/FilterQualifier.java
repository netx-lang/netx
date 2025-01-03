package pascal.netx.compiler.ast.expr.qualifier;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class FilterQualifier extends Qualifier {
    private ExprNode condition;

    private FilterQualifier(ExprNode condition) {
        this.condition = condition;
    }

    public static FilterQualifier of(ExprNode condition) {
        return new FilterQualifier(condition);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        condition.walk(listener);
        listener.exit(this);
    }

    public ExprNode getCondition() {
        return condition;
    }

    public void setCondition(ExprNode condition) {
        this.condition = condition;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (FilterQualifier) obj;
        return Objects.equals(this.condition, that.condition);
    }

    @Override
    public int hashCode() {
        return Objects.hash(condition);
    }

    @Override
    public String toString() {
        return "when " + condition;
    }
}
