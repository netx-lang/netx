package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class InterpolatedSymbol extends PrimarySymbol {
    private final List<String> fragments;
    private final List<ExprNode> exprs;

    private InterpolatedSymbol(List<String> fragments, List<ExprNode> exprs) {
        this.fragments = new LinkedList<>(fragments);
        this.exprs = new LinkedList<>(exprs);
    }

    public static InterpolatedSymbol of(List<String> fragments, List<ExprNode> exprs) {
        return new InterpolatedSymbol(fragments, exprs);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        exprs.forEach(expr -> expr.walk(listener));
        listener.exit(this);
    }

    public List<String> getFragments() {
        return fragments;
    }

    public List<ExprNode> getExprs() {
        return exprs;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InterpolatedSymbol) obj;
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
