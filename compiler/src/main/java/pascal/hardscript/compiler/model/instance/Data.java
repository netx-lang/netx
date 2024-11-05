package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

public sealed interface Data extends Instance permits Clock, DataAccess, Wire {
    interface Factory<T extends Data> extends Entity {
        T create(Identifier id);
    }
}
