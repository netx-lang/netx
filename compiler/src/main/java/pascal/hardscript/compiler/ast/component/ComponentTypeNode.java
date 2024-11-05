package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public class ComponentTypeNode extends ASTNode {
    private ExprNode from;
    private ExprNode to;

    private ComponentTypeNode(ExprNode from, ExprNode to) {
        this.from = from;
        this.to = to;
    }

    public static ComponentTypeNode of(ExprNode from, ExprNode to) {
        return new ComponentTypeNode(from, to);
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentTypeNode that = (ComponentTypeNode) o;
        return Objects.equals(from, that.from) && Objects.equals(to, that.to);
    }

    @Override
    public int hashCode() {
        return Objects.hash(from, to);
    }

    @Override
    public String toString() {
        return from.toString() + " -> " + to.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        from.walk(listener);
        to.walk(listener);
        listener.exit(this);
    }
}
