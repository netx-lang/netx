package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.lang.Identifier;

public sealed interface Data extends Instance permits Clock, DataAccess, Wire {
    interface Factory<T extends Data> extends Entity {
        T create(Identifier id);

        ComponentScope getParent();
    }
}
