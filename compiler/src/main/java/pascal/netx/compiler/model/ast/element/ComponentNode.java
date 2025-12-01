package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends AbstractComponentNode implements PackageElement {
    private ComponentSignatureNode sig;

    private ComponentNode(Identifier identifier, ComponentSignatureNode sig,
                          List<Identifier> attributes, List<ComponentElement> elements) {
        super(identifier, attributes, elements);
        this.sig = sig;
    }

    public static ComponentNode of(Identifier id, ComponentSignatureNode type,
                                   List<Identifier> arguments, List<ComponentElement> elements) {
        return new ComponentNode(id, type, arguments, elements);
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

    public ComponentSignatureNode getSignature() {
        return sig;
    }

    public void setSignature(ComponentSignatureNode sig) {
        this.sig = sig;
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
