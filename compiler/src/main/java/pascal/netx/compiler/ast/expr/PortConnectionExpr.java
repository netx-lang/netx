package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;

import java.util.List;
import java.util.Objects;

public final class PortConnectionExpr extends ExprNode {
    private final List<PortAssign> ports;

    private PortConnectionExpr(List<PortAssign> ports) {
        this.ports = ports;
    }

    public static PortConnectionExpr of(List<PortAssign> ports) {
        return new PortConnectionExpr(ports);
    }

    public List<PortAssign> getPorts() {
        return ports;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        ports.forEach(assign -> {
            assign.lhs.walk(listener);
            assign.rhs.walk(listener);
        });
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PortConnectionExpr portConnectionExpr = (PortConnectionExpr) o;
        return Objects.equals(ports, portConnectionExpr.ports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ports);
    }

    @Override
    public String toString() {
        var list = ports.stream().map(PortAssign::toString).toList();
        return String.format("{ %s }", String.join(", ", list));
    }

    public record PortAssign(ExprNode lhs, ExprNode rhs) {
        @Override
        public String toString() {
            return lhs + " = " + rhs;
        }
    }
}
