package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.util.lang.Identifier;

import java.util.ArrayList;
import java.util.List;

public abstract sealed class AbstractComponentNode extends ASTNode permits ComponentNode, InterfaceNode {
    protected Identifier identifier;
    protected List<Identifier> attributes;
    protected final List<ComponentElement> elements;

    protected AbstractComponentNode(Identifier identifier,
                                    List<Identifier> attributes, List<ComponentElement> elements) {
        this.identifier = identifier;
        this.attributes = attributes;
        this.elements = new ArrayList<>(elements);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public List<Identifier> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Identifier> attributes) {
        this.attributes = attributes;
    }

    public List<ComponentElement> getElements() {
        return elements;
    }
}
