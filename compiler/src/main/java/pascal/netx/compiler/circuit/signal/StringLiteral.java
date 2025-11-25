package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.circuit.Entity;

import javax.annotation.Nonnull;

public record StringLiteral(String value) implements Entity {
    public static StringLiteral of(String value) {
        return new StringLiteral(value);
    }

    @Override
    public @Nonnull String toString() {
        return value;
    }
}
