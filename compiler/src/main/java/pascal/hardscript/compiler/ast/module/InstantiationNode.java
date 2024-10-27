package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class InstantiationNode extends ElementNode {
    private ExprNode type;
    private ExprNode ports;

    private InstantiationNode(ExprNode type, ExprNode ports) {
        this.type = type;
        this.ports = ports;
    }

    public static InstantiationNode of(ExprNode type, ExprNode connection) {
        return new InstantiationNode(type, connection);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        ports.walk(listener);
        listener.exit(this);
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    public ExprNode getPorts() {
        return ports;
    }

    public void setPorts(ExprNode ports) {
        this.ports = ports;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstantiationNode) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.ports, that.ports);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, ports);
    }

    @Override
    public String toString() {
        return "InstantiationNode[" +
                "type=" + type + ", " +
                "connection=" + ports + ']';
    }
}
