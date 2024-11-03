package pascal.hardscript.compiler.ast.expr.selector;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class BitSelector extends Selector {
    private ExprNode index;

    private BitSelector(ExprNode index) {
        this.index = index;
    }

    public static BitSelector of(ExprNode index) {
        return new BitSelector(index);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        index.walk(listener);
        listener.exit(this);
    }

    public ExprNode getIndex() {
        return index;
    }

    public void setIndex(ExprNode index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BitSelector) obj;
        return Objects.equals(this.index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public String toString() {
        return "[" + index + "]";
    }
}
