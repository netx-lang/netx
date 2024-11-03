package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

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
        listener.enter(this);
        for (PrimarySymbol arg : arguments) {
            arg.walk(listener);
        }
        body.walk(listener);
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LambdaExpr that = (LambdaExpr) o;
        return Objects.equals(arguments, that.arguments) && Objects.equals(body, that.body);
    }

    @Override
    public int hashCode() {
        return Objects.hash(arguments, body);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("component");
        if (!arguments.isEmpty()) {
            sb.append(" ").append(arguments.getFirst());
            for (int i = 1; i < arguments.size(); i++) {
                sb.append(", ").append(arguments.get(i));
            }
        }
        sb.append(": ").append(body);
        return sb.toString();
    }
}
