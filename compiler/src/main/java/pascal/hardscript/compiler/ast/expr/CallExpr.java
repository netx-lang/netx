package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Identifier;

import java.util.List;

public record CallExpr(Identifier func, List<ExprNode> arguments) implements ExprNode {

    public static CallExpr of(Identifier func, List<ExprNode> arguments) {
        return new CallExpr(func, arguments);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        arguments.forEach(argument -> argument.walk(listener));
        listener.exit(this);
    }
}
