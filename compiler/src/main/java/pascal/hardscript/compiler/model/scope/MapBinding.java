package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class MapBinding implements Binding {
    private final Map<Identifier, Entity> bindings = new HashMap<>();

    public static MapBinding of(Identifier identifier, Entity entity) {
        var binding = new MapBinding();
        binding.bind(identifier, entity);
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