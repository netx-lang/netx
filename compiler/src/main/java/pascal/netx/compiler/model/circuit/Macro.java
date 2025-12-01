package pascal.netx.compiler.model.circuit;

import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;

public record Macro(ExprNode expr, List<Identifier> identifiers) implements Entity {
    public static Macro of(List<Identifier> identifiers, ExprNode body) {
        return new Macro(body, identifiers);
    }
}
