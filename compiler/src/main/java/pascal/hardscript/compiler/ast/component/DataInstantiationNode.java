package pascal.hardscript.compiler.ast.component;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.expr.ExprNode;
import pascal.hardscript.compiler.ast.expr.PrimarySymbol;
import pascal.hardscript.compiler.ast.expr.SymbolExpr;

import java.util.List;
import java.util.Objects;

public final class DataInstantiationNode extends ElementNode {
    private final List<PrimarySymbol> symbols;
    private final Kind kind;
    private SymbolExpr type;

    private DataInstantiationNode(Kind kind, List<PrimarySymbol> symbols, SymbolExpr type) {
        this.symbols = symbols;
        this.kind = kind;
        this.type = type;
    }

    public static DataInstantiationNode of(Kind kind, List<PrimarySymbol> ids, SymbolExpr type) {
        return new DataInstantiationNode(kind, ids, type);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        symbols.forEach(symbol -> symbol.walk(listener));
        if (type != null) {
            type.walk(listener);
        }
        listener.exit(this);
    }

    public List<PrimarySymbol> getSymbols() {
        return symbols;
    }

    public ExprNode getType() {
        return type;
    }

    public void setType(SymbolExpr type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (DataInstantiationNode) obj;
        return Objects.equals(this.symbols, that.symbols) &&
                Objects.equals(this.type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbols, type);
    }

    @Override
    public String toString() {
        var sb = new StringBuilder();
        sb.append("wire ");
        sb.append(symbols.getFirst());
        for (int i = 1; i < symbols.size(); i++) {
            sb.append(", ").append(symbols.get(i));
        }
        sb.append(" of ");
        sb.append(Objects.requireNonNullElse(type, "auto"));
        return sb.toString();
    }

    public enum Kind {
        WIRE, BUNDLE
    }
}
