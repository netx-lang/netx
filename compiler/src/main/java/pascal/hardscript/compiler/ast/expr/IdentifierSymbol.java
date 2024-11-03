package pascal.hardscript.compiler.ast.expr;

import pascal.hardscript.compiler.ast.ASTListener;
import pascal.hardscript.compiler.ast.Identifier;

import java.util.Objects;

public final class IdentifierSymbol extends PrimarySymbol {
    private Identifier identifier;

    private IdentifierSymbol(Identifier identifier) {
        this.identifier = identifier;
    }

    public static IdentifierSymbol of(Identifier identifier) {
        return new IdentifierSymbol(identifier);
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
        var that = (IdentifierSymbol) obj;
        return Objects.equals(this.identifier, that.identifier);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier);
    }

    @Override
    public String toString() {
        return identifier.string();
    }
}
