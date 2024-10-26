package pascal.hardscript.compiler.ast.module.type;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class BitType extends ComponentTypeNode {
    private ExprNode width;

    private BitType(ExprNode width) {
        this.width = width;
    }

    public static BitType of(ExprNode width) {
        return new BitType(width);
    }

    public ExprNode getWidth() {
        return width;
    }

    public void setWidth(ExprNode width) {
        this.width = width;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BitType) obj;
        return this.width == that.width;
    }

    @Override
    public int hashCode() {
        return Objects.hash(width);
    }

    @Override
    public String toString() {
        return "BitType[" +
                "width=" + width + ']';
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        width.walk(listener);
        listener.exit(this);
    }
}
