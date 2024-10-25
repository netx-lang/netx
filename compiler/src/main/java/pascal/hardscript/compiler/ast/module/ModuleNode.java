package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record ModuleNode(Symbol id, ExprNode type, List<ElementNode> stmts) implements ASTNode {

    public static ModuleNode of(Symbol id, ExprNode type, List<ElementNode> stmts) {
        return new ModuleNode(id, type, stmts);
    }
}
