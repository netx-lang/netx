package pascal.netx.compiler.model.circuit.scope.binding;

import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class IdEntityMap implements Binding, AttrBindList {
    private final LinkedHashMap<Identifier, Entity> map = new LinkedHashMap<>();

    private IdEntityMap() {

    }

    public static IdEntityMap ofEmpty() {
        return new IdEntityMap();
    }

    public static IdEntityMap of(Identifier identifier, Entity entity) {
        var binding = new IdEntityMap();
        binding.bind(identifier, entity);
        return binding;
    }

    public IdEntityMap copyOf() {
        var binding = new IdEntityMap();
        binding.map.putAll(map);
        return binding;
    }

    public void forEach(Consumer<Map.Entry<Identifier, Entity>> consumer) {
        map.entrySet().forEach(consumer);
    }

    @Override
    public void bind(Identifier identifier, Entity entity) {
        map.put(identifier, entity);
    }

    @Override
    public void releaseIf(Predicate<? super Entity> predicate) {
        map.values().removeIf(predicate);
    }

    @Override
    @Nullable
    public Entity lookup(Identifier identifier) {
        return map.get(identifier);
    }

    @Override
    @Nonnull
    public Entity resolve(Identifier identifier) {
        return Objects.requireNonNull(map.get(identifier));
    }

    @Override
    public List<Entity> getArguments() {
        return new ArrayList<>(map.values());
    }

    @Override
    public int size() {
        return map.size();
    }

    @Override
    public boolean isEmpty() {
        return map.isEmpty();
    }

    @Override
    public Entity get(String name, int index) {
        return lookup(Identifier.of(name));
    }

    @Override
    public void forEachEntity(Consumer<Entity> consumer) {
        map.values().forEach(consumer);
    }
}