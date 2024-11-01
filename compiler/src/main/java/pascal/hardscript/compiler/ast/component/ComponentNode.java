package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends ASTNode {
    private Identifier identifier;
    private ExprNode type;
    private final List<Identifier> arguments;
    private final List<ElementNode> elements;

    private ComponentNode(Identifier identifier, ExprNode type,
                          List<Identifier> arguments, List<ElementNode> elements) {
        this.identifier = identifier;
        this.type = type;
        this.arguments = arguments;
        this.elements = elements;
    }

    public static ComponentNode of(Identifier id, ExprNode type,
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

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    public List<ElementNode> getElements() {
        return elements;
    }

    public List<Identifier> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentNode) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.arguments, that.arguments) &&
                Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type, elements);
    }

    @Override
    public String toString() {
        return "Component[" +
                "id=" + identifier + ", " +
                "arguments=" + arguments + ", " +
                "type=" + type + ", " +
                "stmts=" + elements + ']';
    }
}
