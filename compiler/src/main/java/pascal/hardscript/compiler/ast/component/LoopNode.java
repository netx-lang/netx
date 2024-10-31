package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.qualifier.GeneratorQualifier;

import java.util.List;
import java.util.Objects;

public final class LoopNode extends ElementNode {
    private final GeneratorQualifier generator;
    private final List<ElementNode> elements;

    private LoopNode(GeneratorQualifier generator, List<ElementNode> elements) {
        this.generator = generator;
        this.elements = elements;
    }

    public static LoopNode of(GeneratorQualifier iterators, List<ElementNode> elements) {
        return new LoopNode(iterators, elements);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        generator.walk(listener);
        elements.forEach(element -> element.walk(listener));
        listener.exit(this);
    }

    public GeneratorQualifier getGenerator() {
        return generator;
    }

    public List<ElementNode> getElements() {
        return elements;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LoopNode) obj;
        return Objects.equals(this.generator, that.generator) &&
                Objects.equals(this.elements, that.elements);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generator, elements);
    }

    @Override
    public String toString() {
        return "LoopNode[" +
                "iterators=" + generator + ", " +
                "elements=" + elements + ']';
    }
}
