package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class ComponentInstantiationNode extends ElementNode {
    private ExprNode from;
    private ExprNode type;
    private ExprNode to;

    private ComponentInstantiationNode(ExprNode from, ExprNode type, ExprNode to) {
        this.from = from;
        this.type = type;
        this.to = to;
    }

    public static ComponentInstantiationNode of(ExprNode from, ExprNode type, ExprNode to) {
        return new ComponentInstantiationNode(from, type, to);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        from.walk(listener);
        type.walk(listener);
        to.walk(listener);
        listener.exit(this);
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    public ExprNode getFrom() {
        return from;
    }

    public void setFrom(ExprNode from) {
        this.from = from;
    }

    public ExprNode getTo() {
        return to;
    }

    public void setTo(ExprNode to) {
        this.to = to;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentInstantiationNode) obj;
        return Objects.equals(this.from, that.from)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, type, to);
    }

    @Override
    public String toString() {
        return from.toString() + " -> " + type.toString() + " -> " + to.toString();
    }
}
