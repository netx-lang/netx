package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.List;
import java.util.Objects;

public final class ParamSymbolExpr extends ExprNode {
    private SymbolExpr head;
    private final List<ExprNode> arguments;

    private ParamSymbolExpr(SymbolExpr head, List<ExprNode> arguments) {
        this.head = head;
        this.arguments = arguments;
    }

    public static ParamSymbolExpr of(SymbolExpr head, List<ExprNode> arguments) {
        return new ParamSymbolExpr(head, arguments);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        arguments.forEach(argument -> argument.walk(listener));
        listener.exit(this);
    }

    public SymbolExpr getHead() {
        return head;
    }

    public void setHead(SymbolExpr head) {
        this.head = head;
    }

    public List<ExprNode> getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ParamSymbolExpr) obj;
        return Objects.equals(this.head, that.head) &&
                Objects.equals(this.arguments, that.arguments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, arguments);
    }

    @Override
    public String toString() {
        return "CallExpr[" +
                "func=" + head + ", " +
                "arguments=" + arguments + ']';
    }
}
