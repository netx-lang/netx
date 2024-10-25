package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Symbol;

import java.util.Objects;

public final class SymbolExpr extends ExprNode {
    private Symbol symbol;

    private SymbolExpr(Symbol symbol) {
        this.symbol = symbol;
    }

    public static SymbolExpr of(Symbol symbol) {
        return new SymbolExpr(symbol);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public Symbol getSymbol() {
        return symbol;
    }

    public void setSymbol(Symbol symbol) {
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (SymbolExpr) obj;
        return Objects.equals(this.symbol, that.symbol);
    }

    @Override
    public int hashCode() {
        return Objects.hash(symbol);
    }

    @Override
    public String toString() {
        return "SymbolExpr[" +
                "symbol=" + symbol + ']';
    }
}
