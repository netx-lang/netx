package pascal.hardscript.compiler.model.scope.binding;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public sealed interface Binding permits MapBinding, SingleBinding {
    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);
}
