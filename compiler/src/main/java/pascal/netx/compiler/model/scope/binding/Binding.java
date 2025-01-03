package pascal.netx.compiler.model.scope.binding;

import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public sealed interface Binding permits MapBinding, SingleBinding {
    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);
}
