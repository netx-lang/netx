package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MultiBinding implements Binding {
    private final LinkedHashMap<Identifier, Entity> bindings = new LinkedHashMap<>();

    private MultiBinding() {

    }

    public static MultiBinding ofEmpty() {
        return new MultiBinding();
    }

    public static MultiBinding of(Identifier identifier, Entity entity) {
        var binding = new MultiBinding();
        binding.bind(identifier, entity);
        return binding;
    }

    public static MultiBinding of(List<Map.Entry<Identifier, Entity>> entries) {
        var binding = new MultiBinding();
        for (var entry : entries) {
            binding.bind(entry.getKey(), entry.getValue());
        }
        return binding;
    }

    @Override
    public void bind(Identifier identifier, Entity entity) {
        bindings.put(identifier, entity);
    }

    @Override
    public void bind(Binding binding) {
        switch (binding) {
            case MonoBinding single -> bindings.put(single.key(), single.value());
            case MultiBinding map -> bindings.putAll(map.bindings);
        }
    }

    @Override
    public void releaseIf(Predicate<? super Entity> predicate) {
        bindings.values().removeIf(predicate);
    }

    @Override
    @Nullable
    public Entity lookup(Identifier identifier) {
        return bindings.get(identifier);
    }

    @Override
    @Nonnull
    public Entity resolve(Identifier identifier) {
        return Objects.requireNonNull(bindings.get(identifier));
    }

    @Override
    public void forEachEntity(Consumer<Entity> consumer) {
        bindings.values().forEach(consumer);
    }
}