package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

public record LazyType(Scope capture, List<Identifier> identifiers, ExprNode expr) implements Entity {
    public static LazyType of(Scope capture, ExprNode body) {
        return new LazyType(capture, List.of(), body);
    }

    public static LazyType of(Scope capture, List<Identifier> identifiers, ExprNode body) {
        return new LazyType(capture, identifiers, body);
    }
}
