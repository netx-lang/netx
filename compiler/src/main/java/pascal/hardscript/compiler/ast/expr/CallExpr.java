package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.Symbol;

import java.util.List;

public record CallExpr(Symbol func, List<ExprNode> arguments) implements ExprNode {

    public static CallExpr of(Symbol func, List<ExprNode> arguments) {
        return new CallExpr(func, arguments);
    }
}
