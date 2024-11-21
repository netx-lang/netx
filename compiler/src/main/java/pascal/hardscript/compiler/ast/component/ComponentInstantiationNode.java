package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class ComponentInstantiationNode extends ElementNode {
    private boolean declareInPlace;
    private ExprNode lhs;
    private ExprNode rhs;

    private ComponentInstantiationNode(ExprNode lhs, ExprNode rhs, boolean declareInPlace) {
        this.lhs = lhs;
        this.rhs = rhs;
        this.declareInPlace = declareInPlace;
    }

    public static ComponentInstantiationNode of(ExprNode lhs, ExprNode rhs, boolean doLhsDataInstantiation) {
        return new ComponentInstantiationNode(lhs, rhs, doLhsDataInstantiation);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        lhs.walk(listener);
        rhs.walk(listener);
        listener.exit(this);
    }

    public ExprNode getLhs() {
        return lhs;
    }

    public void setLhs(ExprNode lhs) {
        this.lhs = lhs;
    }

    public ExprNode getRhs() {
        return rhs;
    }

    public void setRhs(ExprNode rhs) {
        this.rhs = rhs;
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
        return Objects.equals(this.lhs, that.lhs)
                && Objects.equals(this.rhs, that.rhs)
                && this.declareInPlace == that.declareInPlace;
    }

    @Override
    public int hashCode() {
        return Objects.hash(lhs, rhs, declareInPlace);
    }

    @Override
    public String toString() {
        if (declareInPlace) {
            return "inst " + lhs.toString() + " <- " + rhs.toString();
        }
        return lhs.toString() + " <- " + rhs.toString();
    }
}
