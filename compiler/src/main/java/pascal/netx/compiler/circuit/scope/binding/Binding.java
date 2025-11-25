package pascal.netx.compiler.circuit.scope.binding;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.variable.Data;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.function.Consumer;
import java.util.function.Predicate;

public sealed interface Binding permits MultiBinding, MonoBinding {

    void bind(Identifier identifier, Entity entity);

    void bind(Binding binding);

    void releaseIf(Predicate<? super Entity> predicate);

    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);

    void forEachEntity(Consumer<Entity> consumer);

    default void forEachData(Consumer<Data> consumer) {
        forEachEntity(entity -> {
            if (entity instanceof Data data) {
                consumer.accept(data);
            }
        });
    }
}
