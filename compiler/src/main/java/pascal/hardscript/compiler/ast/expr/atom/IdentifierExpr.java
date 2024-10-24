package pascal.hardscript.compiler.ast.expr.atom;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record IdentifierExpr(Identifier id) implements AtomExpr, ExprNode {

    public static IdentifierExpr of(Identifier id) {
        return new IdentifierExpr(id);
    }
}
