package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class InterfaceNode extends AbstractComponentNode implements PackageElement {
    public static final String OUTPUT_NAME = "$product";
    private ExprNode type;

    private InterfaceNode(Identifier identifier, ExprNode type,
                          List<Identifier> attributes, List<ComponentElement> elements) {
        super(identifier, attributes, elements);
        this.type = type;
    }

    public static InterfaceNode of(Identifier id, ExprNode type,
                                   List<Identifier> attributes,  List<ComponentElement> elements) {
        return new InterfaceNode(id, type, attributes, elements);
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        elements.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        InterfaceNode that = (InterfaceNode) o;
        return Objects.equals(identifier, that.identifier)
                && Objects.equals(attributes, that.attributes)
                && Objects.equals(type, that.type)
                && Objects.equals(elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, attributes, type, elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("interface ").append(identifier);
        sb.append(" : ").append(type);
        sb.append(" {");
        elements.forEach(stmt -> sb.append("\n").append(stmt));
        sb.append("\n}");
        return sb.toString();
    }
}
