package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.expr.list.ListExpr;

public record ModuleTypeNode(ListExpr input, ListExpr output) implements ASTNode {

    public static ModuleTypeNode of(ListExpr input, ListExpr output) {
        return new ModuleTypeNode(input, output);
    }
}
