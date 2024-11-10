package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class ParamSymbolExpr extends SymbolExpr {
    private PrimarySymbol head;
    private final List<ExprNode> arguments;

    private ParamSymbolExpr(PrimarySymbol head, List<ExprNode> arguments) {
        this.head = head;
        this.arguments = new LinkedList<>(arguments);
    }

    public static ParamSymbolExpr of(PrimarySymbol head, List<ExprNode> arguments) {
        return new ParamSymbolExpr(head, arguments);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        arguments.forEach(argument -> argument.walk(listener));
        listener.exit(this);
    }

    public PrimarySymbol getHead() {
        return head;
    }

    public void setHead(PrimarySymbol head) {
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
        var sb = new StringBuilder();
        sb.append(head).append("(");
        if (!arguments.isEmpty()) {
            sb.append(arguments.getFirst());
            for (int i = 1; i < arguments.size(); i++) {
                sb.append(", ").append(arguments.get(i));
            }
        }
        sb.append(")");
        return sb.toString();
    }
}
