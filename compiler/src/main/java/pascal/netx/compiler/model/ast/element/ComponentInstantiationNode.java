package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.expr.ExprNode;

import java.util.Objects;

public final class ComponentInstantiationNode extends ASTNode implements ComponentElement {
    private boolean declareInPlace;
    private ExprNode expr;

    private ComponentInstantiationNode(ExprNode expr, boolean declareInPlace) {
        this.expr = expr;
        this.declareInPlace = declareInPlace;
    }

    public static ComponentInstantiationNode of(ExprNode lhs, boolean doLhsDataInstantiation) {
        return new ComponentInstantiationNode(lhs, doLhsDataInstantiation);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        expr.walk(listener);
        listener.exit(this);
    }

    public ExprNode getExpr() {
        return expr;
    }

    public void setExpr(ExprNode expr) {
        this.expr = expr;
    }

    public boolean needDeclareInPlace() {
        return declareInPlace;
    }

    public void setDeclareInPlace(boolean declareInPlace) {
        this.declareInPlace = declareInPlace;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentInstantiationNode) obj;
        return Objects.equals(this.expr, that.expr)
                && this.declareInPlace == that.declareInPlace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(expr, declareInPlace);
    }

    @Override
    public String toString() {
        if (declareInPlace) {
            return "auto " + expr.toString();
        }
        return expr.toString();
    }
}
