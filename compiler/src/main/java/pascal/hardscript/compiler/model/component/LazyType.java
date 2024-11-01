package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.List;

public record LazyType(Scope capture, List<Identifier> identifiers, ExprNode expr) implements ComponentType {
    public static LazyType of(Scope capture, ExprNode body) {
        return new LazyType(capture, List.of(), body);
    }

    public static LazyType of(Scope capture, List<Identifier> identifiers, ExprNode body) {
        return new LazyType(capture, identifiers, body);
    }
}
