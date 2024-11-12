package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

public record LazyType(List<Identifier> identifiers, ExprNode expr) implements Entity {
    public static LazyType of(ExprNode body) {
        return new LazyType(List.of(), body);
    }

    public static LazyType of(List<Identifier> identifiers, ExprNode body) {
        return new LazyType(identifiers, body);
    }
}
