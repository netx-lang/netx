package pascal.hardscript.compiler.ast.expr.atom;

import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

public record IdentifierExpr(Symbol id) implements AtomExpr, ExprNode {

    public static IdentifierExpr of(Symbol id) {
        return new IdentifierExpr(id);
    }
}
