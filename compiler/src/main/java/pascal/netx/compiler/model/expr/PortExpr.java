package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public final class PortExpr implements Expr {
    private final List<PortAssign> ports = new LinkedList<>();
    private Expr defaultExpr = null;
    private SourceLoc loc = SourceLoc.none();

    @Override
    public @Nonnull String toString() {
        var list = ports.stream().map(PortAssign::toString).toList();
        return String.format("{ %s }", String.join(", ", list));
    }

    public SourceLoc getLoc() {
        return loc;
    }

    public void setLoc(SourceLoc loc) {
        this.loc = loc;
    }

    public List<PortAssign> getPorts() {
        return ports;
    }

    public boolean hasDefault() {
        return defaultExpr != null;
    }

    public Expr getDefaultExpr() {
        return defaultExpr;
    }

    public void setDefaultExpr(Expr defaultExpr) {
        this.defaultExpr = defaultExpr;
    }

    public void addPortAssign(ExprNode port, Expr value) {
        ports.add(PortAssign.of(port, value));
    }

    public record PortAssign(ExprNode port, Expr expr) {
        public static PortAssign of(ExprNode port, Expr value) {
            return new PortAssign(port, value);
        }

        @Override
        public @Nonnull String toString() {
            return port + " : " + expr;
        }
    }
}
