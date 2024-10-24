package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.type.CompTypeNode;

import java.util.List;

public record ComponentNode(List<Symbol> ids, CompTypeNode type) implements ElementNode {

    public static ComponentNode of(List<Symbol> ids, CompTypeNode type) {
        return new ComponentNode(ids, type);
    }
}
