package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends ASTNode {
    private Identifier identifier;
    private ComponentTypeNode type;
    private final List<Identifier> attributes;
    private final List<ElementNode> elements;

    private ComponentNode(Identifier identifier, ComponentTypeNode type,
                          List<Identifier> attributes, List<ElementNode> elements) {
        this.identifier = identifier;
        this.type = type;
        this.attributes = attributes;
        this.elements = elements;
    }

    public static ComponentNode of(Identifier id, ComponentTypeNode type,
                                   List<Identifier> arguments, List<ElementNode> elements) {
        return new ComponentNode(id, type, arguments, elements);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        if (type != null) {
            type.walk(listener);
        }
        elements.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public ComponentTypeNode getType() {
        return type;
    }

    public void setType(ComponentTypeNode type) {
        this.type = type;
    }

    public List<ElementNode> getElements() {
        return elements;
    }

    public List<Identifier> getAttributes() {
        return attributes;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentNode) obj;
        return Objects.equals(this.identifier, that.identifier)
                && Objects.equals(this.type, that.type)
                && Objects.equals(this.attributes, that.attributes)
                && Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type, elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("component ").append(identifier);
        if (type != null) {
            sb.append(" : ").append(type);
        }
        sb.append(" {");
        elements.forEach(stmt -> sb.append("\n").append(stmt));
        sb.append("\n}");
        return sb.toString();
    }
}
