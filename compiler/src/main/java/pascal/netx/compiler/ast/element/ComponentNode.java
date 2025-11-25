package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends AbstractComponentNode implements PackageElement {
    private ComponentSigNode sig;
    private boolean passed;

    private ComponentNode(Identifier identifier, ComponentSigNode sig,
                          List<Identifier> attributes, boolean passed, List<ComponentElement> elements) {
        super(identifier, attributes, elements);
        this.sig = sig;
        this.passed = passed;
    }

    public static ComponentNode of(Identifier id, ComponentSigNode type,
                                   List<Identifier> arguments, List<ComponentElement> elements) {
        return new ComponentNode(id, type, arguments, false, elements);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        if (sig != null) {
            sig.walk(listener);
        }
        elements.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }

    public ComponentSigNode getSig() {
        return sig;
    }

    public void setSig(ComponentSigNode sig) {
        this.sig = sig;
    }

    public boolean isPassed() {
        return passed;
    }

    public void setPassed(boolean passed) {
        this.passed = passed;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentNode) obj;
        return Objects.equals(this.identifier, that.identifier)
                && Objects.equals(this.sig, that.sig)
                && Objects.equals(this.attributes, that.attributes)
                && Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, sig, elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("component ").append(identifier);
        if (sig != null) {
            sb.append(" : ").append(sig);
        }
        sb.append(" {");
        elements.forEach(stmt -> sb.append("\n").append(stmt));
        sb.append("\n}");
        return sb.toString();
    }
}
