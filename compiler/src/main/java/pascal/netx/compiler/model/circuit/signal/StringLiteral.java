package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.model.circuit.Entity;

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
