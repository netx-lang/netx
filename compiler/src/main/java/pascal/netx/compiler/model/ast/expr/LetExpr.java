package pascal.netx.compiler.model.ast.expr;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.element.BindingNode;

import java.util.Objects;

public final class LetExpr extends ExprNode {
    private BindingNode binding;
    private ExprNode body;

    private LetExpr(BindingNode binding, ExprNode body) {
        this.binding = binding;
        this.body = body;
    }

    public static LetExpr of(BindingNode binding, ExprNode body) {
        return new LetExpr(binding, body);
    }

    public BindingNode getBinding() {
        return binding;
    }

    public void setBinding(BindingNode binding) {
        this.binding = binding;
    }

    public ExprNode getBody() {
        return body;
    }

    public void setBody(ExprNode body) {
        this.body = body;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        binding.walk(listener);
        body.walk(listener);
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LetExpr letExpr = (LetExpr) o;
        return Objects.equals(binding, letExpr.binding) && Objects.equals(body, letExpr.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binding, body);
    }

    @Override
    public String toString() {
        return binding + " in " + body;
    }
}
