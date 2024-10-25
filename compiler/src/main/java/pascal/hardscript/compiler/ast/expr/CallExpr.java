package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Identifier;

import java.util.List;
import java.util.Objects;

public final class CallExpr extends ExprNode {
    private Identifier func;
    private final List<ExprNode> arguments;

    private CallExpr(Identifier func, List<ExprNode> arguments) {
        this.func = func;
        this.arguments = arguments;
    }

    public static CallExpr of(Identifier func, List<ExprNode> arguments) {
        return new CallExpr(func, arguments);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        arguments.forEach(argument -> argument.walk(listener));
        listener.exit(this);
    }

    public Identifier getFunc() {
        return func;
    }

    public void setFunc(Identifier func) {
        this.func = func;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CallExpr) obj;
        return Objects.equals(this.func, that.func) &&
                Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(func, arguments);
    }

    @Override
    public String toString() {
        return "CallExpr[" +
                "func=" + func + ", " +
                "arguments=" + arguments + ']';
    }
}
