package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class InterfaceNode extends AbstractComponentNode implements PackageElement {
    public static final String OUTPUT_NAME = "$product";
    private ExprNode sig;

    private InterfaceNode(Identifier identifier, ExprNode sig,
                          List<Identifier> attributes, List<ComponentElement> elements) {
        super(identifier, attributes, elements);
        this.sig = sig;
    }

    public static InterfaceNode of(Identifier id, ExprNode type,
                                   List<Identifier> attributes,  List<ComponentElement> elements) {
        return new InterfaceNode(id, type, attributes, elements);
    }

    public ExprNode getSignature() {
        return sig;
    }

    public void setSignature(ExprNode sig) {
        this.sig = sig;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        sig.walk(listener);
        elements.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InterfaceNode that = (InterfaceNode) o;
        return Objects.equals(identifier, that.identifier)
                && Objects.equals(attributes, that.attributes)
                && Objects.equals(sig, that.sig)
                && Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, attributes, sig, elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("interface ").append(identifier);
        sb.append(" : ").append(sig);
        sb.append(" {");
        elements.forEach(stmt -> sb.append("\n").append(stmt));
        sb.append("\n}");
        return sb.toString();
    }
}
