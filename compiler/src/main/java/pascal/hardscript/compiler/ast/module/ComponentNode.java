package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.PrimarySymbol;

import java.util.List;
import java.util.Objects;

public final class ComponentNode extends ElementNode {
    private final List<PrimarySymbol> symbols;
    private ExprNode type;

    private ComponentNode(List<PrimarySymbol> symbols, ExprNode type) {
        this.symbols = symbols;
        this.type = type;
    }

    public static ComponentNode of(List<PrimarySymbol> ids, ExprNode type) {
        return new ComponentNode(ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        type.walk(listener);
        listener.exit(this);
    }

    public List<PrimarySymbol> getSymbols() {
        return symbols;
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(ExprNode type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (ComponentNode) obj;
        return Objects.equals(this.symbols, that.symbols) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols, type);
    }

    @Override
    public String toString() {
        return "ComponentNode[" +
                "ids=" + symbols + ", " +
                "type=" + type + ']';
    }
}
