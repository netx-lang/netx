package pascal.netx.compiler.model.ast.expr;

import pascal.netx.compiler.model.ast.ASTListener;
import pascal.netx.util.lang.Identifier;

import java.util.Objects;

public final class IdentifierExpr extends SymbolExpr {
    private Identifier identifier;

    private IdentifierExpr(Identifier identifier) {
        this.identifier = identifier;
    }

    public static IdentifierExpr of(Identifier identifier) {
        return new IdentifierExpr(identifier);
    }

    @Override
    public void walk(ASTListener listener) {
        listener.enter(this);
        listener.exit(this);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void setIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (IdentifierExpr) obj;
        return Objects.equals(this.identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier.name();
    }

    @Override
    public Identifier getSymbol() {
        return identifier;
    }
}
