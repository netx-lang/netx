package pascal.hardscript.compiler.ast;

import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;
import java.util.Objects;

public final class Interpolated extends Symbol {
    private final List<String> fragments;
    private final List<ExprNode> exprs;

    private Interpolated(List<String> fragments, List<ExprNode> exprs) {
        this.fragments = fragments;
        this.exprs = exprs;
    }

    public static Interpolated of(List<String> fragments, List<ExprNode> exprs) {
        return new Interpolated(fragments, exprs);
    }

    public List<String> fragments() {
        return fragments;
    }

    public List<ExprNode> exprs() {
        return exprs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Interpolated) obj;
        return Objects.equals(this.fragments, that.fragments) &&
                Objects.equals(this.exprs, that.exprs);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fragments, exprs);
    }

    @Override
    public String toString() {
        return "Interpolated[" +
                "fragments=" + fragments + ", " +
                "exprs=" + exprs + ']';
    }
}
