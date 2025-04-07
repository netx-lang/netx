package pascal.netx.compiler.ast.element;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.compiler.ast.ASTNode;
import pascal.netx.compiler.ast.expr.qualifier.GeneratorQualifier;

import java.util.Objects;

public final class LoopNode extends ASTNode implements ComponentElement {
    private final GeneratorQualifier generator;
    private final ComponentElement element;

    private LoopNode(GeneratorQualifier generator, ComponentElement element) {
        this.generator = generator;
        this.element = element;
    }

    public static LoopNode of(GeneratorQualifier iterators, ComponentElement element) {
        return new LoopNode(iterators, element);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        generator.walk(listener);
        element.walk(listener);
        listener.exit(this);
    }

    public GeneratorQualifier getGenerator() {
        return generator;
    }

    public ComponentElement getElement() {
        return element;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (LoopNode) obj;
        return Objects.equals(this.generator, that.generator) &&
                Objects.equals(this.element, that.element);
    }

    @Override
    public int hashCode() {
        return Objects.hash(generator, element);
    }

    @Override
    public String toString() {
        return String.format("for (%s) {\n%s\n}", generator, element);
    }
}
