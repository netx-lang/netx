package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.Symbol;

import java.util.List;

public record ModuleNode(Symbol id, ModuleTypeNode type, List<ElementNode> stmts) implements ASTNode {

    public static ModuleNode of(Symbol id, ModuleTypeNode type, List<ElementNode> stmts) {
        return new ModuleNode(id, type, stmts);
    }
}
