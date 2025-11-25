package pascal.netx.compiler.circuit;

import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;

public record Macro(List<Identifier> identifiers, ExprNode expr) implements Entity {
    public static Macro of(List<Identifier> identifiers, ExprNode body) {
        return new Macro(identifiers, body);
    }
}
