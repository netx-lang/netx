package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.expr.list.GeneratorQualifier;

import java.util.List;

public record LoopNode(List<GeneratorQualifier> iterators, List<ElementNode> elements) implements ElementNode {

    public static LoopNode of(List<GeneratorQualifier> iterators, List<ElementNode> elements) {
        return new LoopNode(iterators, elements);
    }
}
