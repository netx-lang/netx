package pascal.hardscript.compiler.model.scope.binding;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public sealed interface Binding permits MapBinding, SingleBinding {
    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);

    default MapBinding merge(Binding other) {
        var result = MapBinding.ofEmpty();
        result.add(this);
        result.add(other);
        return result;
    }
}
