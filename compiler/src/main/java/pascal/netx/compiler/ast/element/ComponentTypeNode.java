package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.expr.ExprNode;

import java.util.Objects;

public class ComponentTypeNode extends ASTNode {
    private ExprNode input;
    private ExprNode output;

    private ComponentTypeNode(ExprNode input, ExprNode output) {
        this.input = input;
        this.output = output;
    }

    public static ComponentTypeNode of(ExprNode from, ExprNode to) {
        return new ComponentTypeNode(from, to);
    }

    public ExprNode getInput() {
        return input;
    }

    public void setInput(ExprNode input) {
        this.input = input;
    }

    public ExprNode getOutput() {
        return output;
    }

    public void setOutput(ExprNode output) {
        this.output = output;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ComponentTypeNode that = (ComponentTypeNode) o;
        return Objects.equals(input, that.input) && Objects.equals(output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(input, output);
    }

    @Override
    public String toString() {
        return input.toString() + " -> " + output.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        input.walk(listener);
        output.walk(listener);
        listener.exit(this);
    }
}
