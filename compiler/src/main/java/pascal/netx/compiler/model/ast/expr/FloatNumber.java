package pascal.netx.compiler.model.ast.expr;

import pascal.netx.compiler.model.ast.ASTListener;

import java.math.BigDecimal;
import java.util.Objects;

public final class FloatNumber extends NumberExpr {
    private final int size;
    private final BigDecimal value;

    private FloatNumber(int size, BigDecimal value) {
        this.size = size;
        this.value = value;
    }

    public static FloatNumber of(String value) {
        var literal = value.replaceAll("_", "");
        if (value.contains("'")) {
            var parts = literal.toLowerCase().split("'");
            return new FloatNumber(Integer.parseInt(parts[0]), new BigDecimal(parts[1].substring(1)));
        } else {
            return new FloatNumber(32, new BigDecimal(literal));
        }
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public int getSize() {
        return size;
    }

    public BigDecimal getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        FloatNumber that = (FloatNumber) o;
        return size == that.size && Objects.equals(value, that.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, value);
    }

    @Override
    public String toString() {
        return size + "'f" + value.toString();
    }
}
