package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;

import java.util.List;
import java.util.Objects;

public final class LoopNode extends ASTNode implements ComponentElement {
    private final GeneratorQualifier generator;
    private final List<ComponentElement> elements;

    private LoopNode(GeneratorQualifier generator, List<ComponentElement> elements) {
        this.generator = generator;
        this.elements = elements;
    }

    public static LoopNode of(GeneratorQualifier iterators, List<ComponentElement> elements) {
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

    public List<ComponentElement> getElements() {
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
        var sb = new StringBuilder();
        sb.append("for (");
        sb.append(generator);
        sb.append(") {\n");
        for (var element : elements) {
            sb.append(element).append('\n');
        }
        sb.append("}");
        return sb.toString();
    }
}
