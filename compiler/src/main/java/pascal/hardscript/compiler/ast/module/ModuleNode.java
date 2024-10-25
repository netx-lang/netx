package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;

public record ModuleNode(Identifier id, ExprNode type, List<ElementNode> stmts) implements ASTNode {

    public static ModuleNode of(Identifier id, ExprNode type, List<ElementNode> stmts) {
        return new ModuleNode(id, type, stmts);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        stmts.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }
}
