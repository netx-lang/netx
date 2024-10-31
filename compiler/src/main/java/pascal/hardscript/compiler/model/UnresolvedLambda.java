package pascal.hardscript.compiler.model;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.List;

public record UnresolvedLambda(Scope capture, List<Identifier> identifiers, ExprNode body) implements Entity {
    public static UnresolvedLambda of(Scope capture, List<Identifier> identifiers, ExprNode body) {
        return new UnresolvedLambda(capture, identifiers, body);
    }
}
