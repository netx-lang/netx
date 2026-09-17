package pascal.netx.compiler.model.circuit;

import pascal.netx.compiler.model.ast.expr.ExprNode;
import pascal.netx.util.lang.Identifier;

import java.util.List;
import java.util.Map;

/** A lazy expression with definition-site bindings; self is present only for parameterized macros. */
public record Macro(ExprNode expr, List<Identifier> identifiers,
                    Map<Identifier, Entity> captures, Identifier self) implements Entity {
    public Macro {
        identifiers = List.copyOf(identifiers);
        captures = Map.copyOf(captures);
    }
}
