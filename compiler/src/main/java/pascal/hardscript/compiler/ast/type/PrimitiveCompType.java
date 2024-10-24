package pascal.hardscript.compiler.ast.type;

import pascal.hardscript.compiler.ast.expr.AtomExpr;

public record PrimitiveCompType(AtomExpr expr) implements CompTypeNode {

    public static PrimitiveCompType of(AtomExpr expr) {
        return new PrimitiveCompType(expr);
    }
}
