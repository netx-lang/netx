package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class MapBinding implements Binding {
    private final LinkedHashMap<Identifier, Entity> bindings = new LinkedHashMap<>();

    private MapBinding() {

    }

    public static MapBinding ofEmpty() {
        return new MapBinding();
    }

    public static MapBinding of(Identifier identifier, Entity entity) {
        var binding = new MapBinding();
        binding.bind(identifier, entity);
        return binding;
    }

    public static MapBinding of(List<Map.Entry<Identifier, Entity>> entries) {
        var binding = new MapBinding();
        for (var entry : entries) {
            binding.bind(entry.getKey(), entry.getValue());
        }
        return binding;
    }

    public void bind(Identifier identifier, Entity entity) {
        bindings.put(identifier, entity);
    }

    public void add(Binding binding) {
        switch (binding) {
            case SingleBinding single -> bindings.put(single.key(), single.value());
            case MapBinding map -> bindings.putAll(map.bindings);
        }
    }

    public Collection<Entity> getEntities() {
        return bindings.values();
    }

    public Set<Map.Entry<Identifier, Entity>> getEntries() {
        return bindings.entrySet();
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
}