package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.expr.list.GeneratorQual;

import java.util.List;

public record LoopNode(List<GeneratorQual> iterators, List<ElementNode> elements) implements ElementNode {

    public static LoopNode of(List<GeneratorQual> iterators, List<ElementNode> elements) {
        return new LoopNode(iterators, elements);
    }
}
