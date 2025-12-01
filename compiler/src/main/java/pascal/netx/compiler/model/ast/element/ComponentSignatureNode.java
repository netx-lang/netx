package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;
import pascal.netx.compiler.model.ast.expr.ExprNode;

import java.util.Objects;

public class ComponentSignatureNode extends ASTNode {
    private ExprNode input;
    private ExprNode output;

    private ComponentSignatureNode(ExprNode input, ExprNode output) {
        this.input = input;
        this.output = output;
    }

    public static ComponentSignatureNode of(ExprNode from, ExprNode to) {
        return new ComponentSignatureNode(from, to);
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
        ComponentSignatureNode that = (ComponentSignatureNode) o;
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
