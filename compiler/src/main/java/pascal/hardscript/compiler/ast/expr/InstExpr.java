package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.module.InstantiationNode;

public record InstExpr(InstantiationNode instantiation) implements ExprNode {

    public static InstExpr of(InstantiationNode instantiation) {
        return new InstExpr(instantiation);
    }
}
