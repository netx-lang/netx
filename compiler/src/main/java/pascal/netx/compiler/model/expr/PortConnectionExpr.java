package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.ast.expr.ExprNode;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public record PortConnectionExpr(SourceLoc loc, InstantiationRef instance, List<PortAssign> ports) implements Expr {
    public static PortConnectionExpr of(SourceLoc loc, InstantiationRef instance, List<PortAssign> ports) {
        return new PortConnectionExpr(loc, instance, ports);
    }

    @Override
    public String toString() {
        return instance + "<+> {" + ports + "}";
    }

    public record PortAssign(ExprNode lhs, Expr rhs) {
        public static PortAssign of(ExprNode port, Expr value) {
            return new PortAssign(port, value);
        }

        @Override
        public String toString() {
            return lhs + " = " + rhs;
        }
    }
}
