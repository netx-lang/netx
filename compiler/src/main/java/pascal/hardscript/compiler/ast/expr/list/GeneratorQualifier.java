package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Symbol;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class GeneratorQualifier extends Qualifier {
    private Symbol head;
    private ExprNode from;

    private GeneratorQualifier(Symbol head, ExprNode from) {
        this.head = head;
        this.from = from;
    }

    public static GeneratorQualifier of(Symbol head, ExprNode from) {
        return new GeneratorQualifier(head, from);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        from.walk(listener);
        listener.exit(this);
    }

    public Symbol getHead() {
        return head;
    }

    public void setHead(Symbol head) {
        this.head = head;
    }

    public ExprNode getFrom() {
        return from;
    }

    public void setFrom(ExprNode from) {
        this.from = from;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (GeneratorQualifier) obj;
        return Objects.equals(this.head, that.head) &&
                Objects.equals(this.from, that.from);
    }

    @Override
    public int hashCode() {
        return Objects.hash(head, from);
    }

    @Override
    public String toString() {
        return "GeneratorQualifier[" +
                "head=" + head + ", " +
                "from=" + from + ']';
    }
}
