package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

public record Macro(List<Identifier> identifiers, ExprNode expr) implements Entity {
    public static Macro of(ExprNode body) {
        return new Macro(List.of(), body);
    }

    public static Macro of(List<Identifier> identifiers, ExprNode body) {
        return new Macro(identifiers, body);
    }
}
