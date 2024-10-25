package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.list.GeneratorQualifier;

import java.util.List;

public record LoopNode(List<GeneratorQualifier> iterators, List<ElementNode> elements) implements ElementNode {

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
}
