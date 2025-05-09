package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public record PortExpr(SourceLoc loc, List<PortAssign> ports) implements Expr {
    public static PortExpr of(SourceLoc loc, List<PortAssign> ports) {
        return new PortExpr(loc, ports);
    }

    @Override
    public String toString() {
        var list = ports.stream().map(PortAssign::toString).toList();
        return String.format("{ %s }", String.join(", ", list));
    }

    public record PortAssign(ExprNode port, Expr expr) {
        public static PortAssign of(ExprNode port, Expr value) {
            return new PortAssign(port, value);
        }

        @Override
        public String toString() {
            return port + " : " + expr;
        }
    }
}
