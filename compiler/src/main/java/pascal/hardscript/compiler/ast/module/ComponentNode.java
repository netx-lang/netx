package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.type.CompTypeNode;

import java.util.List;

public record ComponentNode(List<Identifier> ids, CompTypeNode type) implements ElementNode {

    public static ComponentNode of(List<Identifier> ids, CompTypeNode type) {
        return new ComponentNode(ids, type);
    }
}
