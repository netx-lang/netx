package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;

import java.util.Objects;

public final class IntNumber extends NumberExpr {
    private int value;

    private IntNumber(int value) {
        this.value = value;
    }

    public static IntNumber of(int value) {
        return new IntNumber(value);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IntNumber) obj;
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
