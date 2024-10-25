package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.expr.ExprNode;

public record InstantiationNode(ExprNode type, ExprNode connection) implements ElementNode {

    public static InstantiationNode of(ExprNode type, ExprNode connection) {
        return new InstantiationNode(type, connection);
    }
}
