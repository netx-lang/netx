package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public final class Binding {
    private final Map<Identifier, Entity> bindings = new HashMap<>();

    public static Binding of(Identifier identifier, Entity entity) {
        var binding = new Binding();
        binding.bind(identifier, entity);
        return binding;
    }

    public void bind(Identifier identifier, Entity entity) {
        bindings.put(identifier, entity);
    }

    @Nullable
    public Entity lookup(Identifier identifier) {
        return bindings.get(identifier);
    }

    @Nonnull
    public Entity resolve(Identifier identifier) {
        return Objects.requireNonNull(bindings.get(identifier));
    }
}