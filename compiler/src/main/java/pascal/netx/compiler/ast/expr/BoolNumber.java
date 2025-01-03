package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;

import java.util.Objects;

public final class BoolNumber extends NumberExpr {
    private boolean value;

    private BoolNumber(boolean value) {
        this.value = value;
    }

    public static BoolNumber of(boolean value) {
        return new BoolNumber(value);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public boolean getValue() {
        return value;
    }

    public void setValue(boolean value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (BoolNumber) obj;
        return this.value == that.value;
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

}
