package pascal.hardscript.compiler.ast.expr.qualifier;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.module.Binding;

import java.util.Objects;

public final class BindingQualifier extends Qualifier {
    private Binding binding;

    private BindingQualifier(Binding binding) {
        this.binding = binding;
    }

    public static BindingQualifier of(Binding binding) {
        return new BindingQualifier(binding);
    }

    public Binding getBinding() {
        return binding;
    }

    public void setBinding(Binding binding) {
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
        return "BindingQualifier[" +
                "binding=" + binding + ']';
    }

    @Override
    public void walk(ASTListener listener) {

    }
}
