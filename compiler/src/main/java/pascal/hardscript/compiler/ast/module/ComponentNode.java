package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;
import pascal.hardscript.compiler.ast.module.type.ComponentTypeNode;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends ElementNode {
    private final List<SymbolExpr> identifiers;
    private ComponentTypeNode type;

    private ComponentNode(List<SymbolExpr> identifiers, ComponentTypeNode type) {
        this.identifiers = identifiers;
        this.type = type;
    }

    public static ComponentNode of(List<SymbolExpr> ids, ComponentTypeNode type) {
        return new ComponentNode(ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        identifiers.forEach(id -> id.walk(listener));
        type.walk(listener);
        listener.exit(this);
    }

    public List<SymbolExpr> getIdentifiers() {
        return identifiers;
    }

    public ComponentTypeNode getType() {
        return type;
    }

    public void setType(ComponentTypeNode type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentNode) obj;
        return Objects.equals(this.identifiers, that.identifiers) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifiers, type);
    }

    @Override
    public String toString() {
        return "ComponentNode[" +
                "ids=" + identifiers + ", " +
                "type=" + type + ']';
    }
}
