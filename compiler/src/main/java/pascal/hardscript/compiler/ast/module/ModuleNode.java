package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.ASTNode;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.expr.ExprNode;

import java.util.List;
import java.util.Objects;

public final class ModuleNode extends ASTNode {
    private Identifier identifier;
    private ExprNode type;
    private final List<ElementNode> stmts;

    private ModuleNode(Identifier identifier, ExprNode type, List<ElementNode> stmts) {
        this.identifier = identifier;
        this.type = type;
        this.stmts = stmts;
    }

    public static ModuleNode of(Identifier id, ExprNode type, List<ElementNode> stmts) {
        return new ModuleNode(id, type, stmts);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        stmts.forEach(stmt -> stmt.walk(listener));
        listener.exit(this);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    public List<ElementNode> getStmts() {
        return stmts;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ModuleNode) obj;
        return Objects.equals(this.identifier, that.identifier) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.stmts, that.stmts);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, type, stmts);
    }

    @Override
    public String toString() {
        return "ModuleNode[" +
                "id=" + identifier + ", " +
                "type=" + type + ", " +
                "stmts=" + stmts + ']';
    }
}
