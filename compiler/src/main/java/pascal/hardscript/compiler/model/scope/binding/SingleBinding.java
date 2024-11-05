package pascal.hardscript.compiler.model.scope.binding;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record SingleBinding(Identifier key, Entity value) implements Binding {

    public static SingleBinding of(Identifier key, Entity value) {
        return new SingleBinding(key, value);
    }

    @Override
    @Nullable
    public Entity lookup(Identifier identifier) {
        return identifier.equals(key) ? value : null;
    }


    @Override
    @Nonnull
    public Entity resolve(Identifier identifier) {
        if (identifier.equals(key)) {
            return value;
        }
        throw new IllegalStateException("Cannot resolve " + identifier);
    }
}
