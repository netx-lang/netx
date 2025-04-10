package pascal.netx.compiler.ast.expr;

import pascal.netx.compiler.ast.ASTListener;

import java.util.List;
import java.util.Objects;

public final class NamedSequentialExpr extends ExprNode {
    private final List<PortAssign> ports;
    private SymbolExpr instance;

    private NamedSequentialExpr(SymbolExpr instance, List<PortAssign> ports) {
        this.instance = instance;
        this.ports = ports;
    }

    public static NamedSequentialExpr of(SymbolExpr instance, List<PortAssign> ports) {
        return new NamedSequentialExpr(instance, ports);
    }

    public SymbolExpr getInstance() {
        return instance;
    }

    public void setInstance(SymbolExpr instance) {
        this.instance = instance;
    }

    public List<PortAssign> getPorts() {
        return ports;
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        instance.walk(listener);
        ports.forEach(assign -> {
            assign.lhs.walk(listener);
            assign.rhs.walk(listener);
        });
        listener.exit(this);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NamedSequentialExpr namedSequentialExpr = (NamedSequentialExpr) o;
        return Objects.equals(instance, namedSequentialExpr.instance) && Objects.equals(ports, namedSequentialExpr.ports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(instance, ports);
    }

    @Override
    public String toString() {
        return instance.toString() + " <+> { " +
                String.join(", ", ports.stream().map(PortAssign::toString).toList())
                + " }";
    }

    public record PortAssign(ExprNode lhs, ExprNode rhs) {
        @Override
        public String toString() {
            return lhs + " = " + rhs;
        }
    }
}
