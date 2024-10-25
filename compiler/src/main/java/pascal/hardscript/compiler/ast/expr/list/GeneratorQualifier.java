package pascal.hardscript.compiler.ast.expr.list;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

import java.util.Objects;

public final class GeneratorQualifier extends Qualifier {
    private SymbolExpr head;
    private ExprNode from;

    private GeneratorQualifier(SymbolExpr head, ExprNode from) {
        this.head = head;
        this.from = from;
    }

    public static GeneratorQualifier of(SymbolExpr head, ExprNode from) {
        return new GeneratorQualifier(head, from);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        head.walk(listener);
        from.walk(listener);
        listener.exit(this);
    }

    public SymbolExpr getHead() {
        return head;
    }

    public void setHead(SymbolExpr head) {
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
