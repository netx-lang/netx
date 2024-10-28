package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Binding {
    @Nullable
    Entity lookup(Identifier identifier);

    @Nonnull
    Entity resolve(Identifier identifier);
}
