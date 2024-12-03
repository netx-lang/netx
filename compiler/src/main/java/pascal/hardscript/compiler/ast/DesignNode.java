package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.component.BindingNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.util.type.Either;
import pascal.hardscript.util.type.Left;
import pascal.hardscript.util.type.Right;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class DesignNode extends ASTNode {
    private final List<Either<BindingNode, ComponentNode>> elements = new ArrayList<>();

    private DesignNode() {
    }

    public static DesignNode of() {
        return new DesignNode();
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        elements.forEach(either -> {
            switch (either) {
                case Left(var left) -> left.walk(listener);
                case Right(var right) -> right.walk(listener);
            }
        });
        listener.exit(this);
    }

    public List<Either<BindingNode, ComponentNode>> getElements() {
        return elements;
    }

    public void addDefinition(BindingNode binding) {
        elements.add(Either.makeLeft(binding));
    }

    public void addDefinition(ComponentNode component) {
        elements.add(Either.makeRight(component));
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DesignNode) obj;
        return Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(elements);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        for (var definition : elements) {
            sb.append(definition.map(
                    BindingNode::toString,
                    ComponentNode::toString
            )).append('\n');
        }
        return sb.toString();
    }
}
