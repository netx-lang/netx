package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.qualifier.Qualifier;

import java.util.List;
import java.util.Objects;

public final class ListComprehension extends ListExpr {
    private ExprNode head;
    private final List<Qualifier> qualifiers;

    private ListComprehension(ExprNode head, List<Qualifier> qualifiers) {
        this.head = head;
        this.qualifiers = qualifiers;
    }

    public static ListComprehension of(ExprNode head, List<Qualifier> qualifiers) {
        return new ListComprehension(head, qualifiers);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        qualifiers.forEach(qualifier -> qualifier.walk(listener));
        listener.exit(this);
    }

    public ExprNode getHead() {
        return head;
    }

    public void setHead(ExprNode head) {
        this.head = head;
    }

    public List<Qualifier> getQualifiers() {
        return qualifiers;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ListComprehension) obj;
        return Objects.equals(this.head, that.head) &&
                Objects.equals(this.qualifiers, that.qualifiers);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, qualifiers);
    }

    @Override
    public String toString() {
        return "ListComprehension[" +
                "head=" + head + ", " +
                "qualifiers=" + qualifiers + ']';
    }
}
