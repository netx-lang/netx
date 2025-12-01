package pascal.netx.compiler.model.ast.expr.qualifier;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.element.BindingNode;

import java.util.Objects;

public final class BindingQualifier extends Qualifier {
    private BindingNode binding;

    private BindingQualifier(BindingNode binding) {
        this.binding = binding;
    }

    public static BindingQualifier of(BindingNode binding) {
        return new BindingQualifier(binding);
    }

    public BindingNode getBinding() {
        return binding;
    }

    public void setBinding(BindingNode binding) {
        this.binding = binding;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BindingQualifier) obj;
        return Objects.equals(this.binding, that.binding);
    }

    @Override
    public int hashCode() {
        return Objects.hash(binding);
    }

    @Override
    public String toString() {
        return binding.toString();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        binding.walk(listener);
        listener.exit(this);
    }
}
