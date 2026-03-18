package pascal.netx.compiler.model.circuit.scope.binding;

import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public sealed interface Binding permits IdEntityMap {

    void bind(Identifier identifier, Entity entity);

    void releaseIf(Predicate<? super Entity> predicate);

    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);

    void forEachEntity(Consumer<Entity> consumer);
}
