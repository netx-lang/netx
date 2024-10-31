package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;

public sealed interface Data extends Entity permits Clock, DataAccess, Wire {
    Scope getParent();

    default int getDepth() {
        if (getParent() instanceof ComponentScope module) {
            return module.getDepth();
        } else {
            return getParent().getUpperComponent().getDepth();
        }
    }

    int getWidth();

    Identifier getIdentifier();

    interface Factory<T extends Data> extends Entity {
        T create(Identifier id);
    }

    default boolean isBuiltin() {
        return getIdentifier().string().startsWith("$");
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }
}
