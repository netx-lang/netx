package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.type.Pair;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class CallExpr extends SymbolExpr {
    private Identifier head;
    private final List<Pair<Identifier, ExprNode>> argAssigns;

    private CallExpr(Identifier head, List<Pair<Identifier, ExprNode>> argAssigns) {
        this.head = head;
        this.argAssigns = new LinkedList<>(argAssigns);
    }

    public static CallExpr ofEmpty(Identifier head) {
        return new CallExpr(head, List.of());
    }

    public static CallExpr fromOrder(Identifier head, List<ExprNode> arguments) {
        var assigns = arguments.stream().map(e -> Pair.of((Identifier) null, e)).toList();
        return new CallExpr(head, assigns);
    }

    public static CallExpr fromAssign(Identifier head, List<Pair<Identifier, ExprNode>> argAssigns) {
        return new CallExpr(head, argAssigns);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        getArguments().forEach(arg -> arg.walk(listener));
        listener.exit(this);
    }

    public Identifier getHead() {
        return head;
    }

    public void setHead(Identifier head) {
        this.head = head;
    }

    public List<Pair<Identifier, ExprNode>> getAssigns() {
        return argAssigns;
    }

    public List<ExprNode> getArguments() {
        return argAssigns.stream().map(Pair::second).toList();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (CallExpr) obj;
        return Objects.equals(this.head, that.head) &&
                Objects.equals(this.argAssigns, that.argAssigns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, argAssigns);
    }

    private static String valueOf(Pair<Identifier, ExprNode> assign) {
        if (assign.first() == null) {
            return assign.second().toString();
        } else {
            return assign.first() + " = " + assign.second();
        }
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append(head).append("(");
        if (!argAssigns.isEmpty()) {
            sb.append(valueOf(argAssigns.getFirst()));
            for (int i = 1; i < argAssigns.size(); i++) {
                sb.append(", ").append(valueOf(argAssigns.get(i)));
            }
        }
        sb.append(")");
        return sb.toString();
    }

    @Override
    public Identifier getSymbol() {
        return head;
    }
}
