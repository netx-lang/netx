package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends ASTNode implements PackageElement {
    private Kind kind;
    private Identifier identifier;
    private List<Identifier> attributes;
    private ComponentTypeNode type;
    private boolean passed;
    private List<ComponentElement> elements;

    private ComponentNode(Kind kind, Identifier identifier, ComponentTypeNode type,
                          List<Identifier> attributes, boolean passed, List<ComponentElement> elements) {
        this.identifier = identifier;
        this.type = type;
        this.attributes = attributes;
        this.elements = elements;
        this.kind = kind;
        this.passed = passed;
    }

    public static ComponentNode of(Kind kind, Identifier id, ComponentTypeNode type,
                                   List<Identifier> arguments, List<ComponentElement> elements) {
        return new ComponentNode(kind, id, type, arguments, false, elements);
    }

    public static ComponentNode ofPassed(Kind kind, Identifier id, ComponentTypeNode type,
                                   List<Identifier> arguments) {
        return new ComponentNode(kind, id, type, arguments, true, List.of());
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

    public List<ComponentElement> getElements() {
        return elements;
    }

    public void setElements(List<ComponentElement> elements) {
        this.elements = elements;
    }

    public List<Identifier> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Identifier> attributes) {
        this.attributes = attributes;
    }

    public void setKind(Kind kind) {
        this.kind = kind;
    }

    public Kind getKind() {
        return kind;
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

    public enum Kind {
        COMPONENT, INTERFACE,
    }
}
