package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MultiBinding implements Binding, ArgBindList {
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

    public MultiBinding copyOf() {
        var binding = new MultiBinding();
        binding.bindings.putAll(bindings);
        return binding;
    }

    public void forEach(Consumer<Map.Entry<Identifier, Entity>> consumer) {
        bindings.entrySet().forEach(consumer);
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
    public List<Entity> getArguments() {
        return new ArrayList<>(bindings.values());
    }

    @Override
    public int size() {
        return bindings.size();
    }

    @Override
    public boolean isEmpty() {
        return bindings.isEmpty();
    }

    @Override
    public void forEachEntity(Consumer<Entity> consumer) {
        bindings.values().forEach(consumer);
    }
}