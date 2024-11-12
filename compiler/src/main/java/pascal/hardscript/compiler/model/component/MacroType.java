package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

public record MacroType(List<Identifier> identifiers, ExprNode expr) implements Entity {
    public static MacroType of(ExprNode body) {
        return new MacroType(List.of(), body);
    }

    public static MacroType of(List<Identifier> identifiers, ExprNode body) {
        return new MacroType(identifiers, body);
    }
}
