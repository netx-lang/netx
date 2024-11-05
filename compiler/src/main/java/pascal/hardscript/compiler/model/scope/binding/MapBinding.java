package pascal.hardscript.compiler.model.scope.binding;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public final class MapBinding implements Binding {
    private final Map<Identifier, Entity> bindings = new HashMap<>();

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

    public Collection<Entity> getEntities() {
        return bindings.values();
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