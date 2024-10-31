package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.LinkedList;
import java.util.List;

public final class LambdaExpr extends ExprNode {
    private final List<PrimarySymbol> arguments;
    private ExprNode body;

    private LambdaExpr(List<PrimarySymbol> arguments, ExprNode body) {
        this.arguments = new LinkedList<>(arguments);
        this.body = body;
    }

    public static LambdaExpr of(List<PrimarySymbol> arguments, ExprNode expr) {
        return new LambdaExpr(arguments, expr);
    }

    public List<PrimarySymbol> getArguments() {
        return arguments;
    }

    public ExprNode getBody() {
        return body;
    }

    public void setBody(ExprNode body) {
        this.body = body;
    }

    @Override
    public void walk(ASTListener listener) {

    }
}
