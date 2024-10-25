package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.list.GeneratorQualifier;

import java.util.List;
import java.util.Objects;

public final class LoopNode extends ElementNode {
    private final List<GeneratorQualifier> iterators;
    private final List<ElementNode> elements;

    private LoopNode(List<GeneratorQualifier> iterators, List<ElementNode> elements) {
        this.iterators = iterators;
        this.elements = elements;
    }

    public static LoopNode of(List<GeneratorQualifier> iterators, List<ElementNode> elements) {
        return new LoopNode(iterators, elements);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        iterators.forEach(iterator -> iterator.walk(listener));
        elements.forEach(element -> element.walk(listener));
        listener.exit(this);
    }

    public List<GeneratorQualifier> getIterators() {
        return iterators;
    }

    public List<ElementNode> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LoopNode) obj;
        return Objects.equals(this.iterators, that.iterators) &&
                Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(iterators, elements);
    }

    @Override
    public String toString() {
        return "LoopNode[" +
                "iterators=" + iterators + ", " +
                "elements=" + elements + ']';
    }
}
