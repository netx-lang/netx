package pascal.netx.compiler.model.circuit.scope.binding;

import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public record MonoBinding(Identifier key, Entity value) implements Binding {

    public static MonoBinding of(Identifier key, Entity value) {
        return new MonoBinding(key, value);
    }

    @Override
    public void bind(Identifier identifier, Entity entity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void bind(Binding binding) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void releaseIf(Predicate<? super Entity> predicate) {
        throw new UnsupportedOperationException();
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

    @Override
    public void forEachEntity(Consumer<Entity> consumer) {
        consumer.accept(value);
    }
}
