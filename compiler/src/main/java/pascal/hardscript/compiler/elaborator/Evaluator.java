package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.Interpolated;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.model.meta.ListValue;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.TodoException;

public final class Evaluator {
    private final Scope scope;

    public Evaluator(Scope scope) {
        this.scope = scope;
    }

    public String evaluateToString(ExprNode expr) {
        throw new TodoException();
    }

    public ListValue evaluateToList(ExprNode expr) {
        throw new TodoException();
    }

    public Identifier evaluateSymbol(Symbol symbol) {
        return switch (symbol) {
            case Identifier id -> id;
            case Interpolated interpolated -> evaluateInterpolation(interpolated);
        };
    }

    private Identifier evaluateInterpolation(Interpolated interpolated) {
        var sb = new StringBuilder();
        var exprIter = interpolated.exprs().iterator();
        for (var fragment : interpolated.fragments()) {
            sb.append(fragment);
            sb.append(evaluateToString(exprIter.next()));
        }
        exprIter.forEachRemaining(expr -> sb.append(evaluateToString(expr)));
        var result = Identifier.of(sb.toString());
        result.setSourceLoc(interpolated.getSourceLoc());
        return result;
    }
}
