package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.Objects;

public final class InstantiationNode extends ElementNode {
    private ExprNode type;
    private ExprNode connection;

    private InstantiationNode(ExprNode type, ExprNode connection) {
        this.type = type;
        this.connection = connection;
    }

    public static InstantiationNode of(ExprNode type, ExprNode connection) {
        return new InstantiationNode(type, connection);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        connection.walk(listener);
        listener.exit(this);
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    public ExprNode getConnection() {
        return connection;
    }

    public void setConnection(ExprNode connection) {
        this.connection = connection;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (InstantiationNode) obj;
        return Objects.equals(this.type, that.type) &&
                Objects.equals(this.connection, that.connection);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, connection);
    }

    @Override
    public String toString() {
        return "InstantiationNode[" +
                "type=" + type + ", " +
                "connection=" + connection + ']';
    }
}
