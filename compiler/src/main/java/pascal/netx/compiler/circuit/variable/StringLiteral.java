package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;

public record StringLiteral(SourceLoc loc, String value) implements Entity {
    public static StringLiteral of(SourceLoc loc, String value) {
        return new StringLiteral(loc, value);
    }

    @Override
    public @Nonnull String toString() {
        return value;
    }
}
