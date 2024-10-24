package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.Identifier;

public record IdentifierExpr(Identifier id) implements AtomExpr, ExprNode {

    public static IdentifierExpr of(Identifier id) {
        return new IdentifierExpr(id);
    }
}
