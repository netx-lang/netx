package pascal.netx.compiler.model.ast.element;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.compiler.model.ast.ASTNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class BlockNode extends ASTNode implements ComponentElement {
    private final List<ComponentElement> elements = new ArrayList<>();

    private BlockNode(List<ComponentElement> elements) {
        this.elements.addAll(elements);
    }

    public static BlockNode of(List<ComponentElement> elements) {
        return new BlockNode(elements);
    }

    public List<ComponentElement> getElements() {
        return elements;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        for (ComponentElement element : elements) {
            element.walk(listener);
        }
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BlockNode blockNode = (BlockNode) o;
        return Objects.equals(elements, blockNode.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(elements);
    }

    @Override
    public String toString() {
        return String.join("\n", elements.stream().map(Object::toString).toList());
    }
}
