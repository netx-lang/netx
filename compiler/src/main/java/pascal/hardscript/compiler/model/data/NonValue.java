package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

public sealed interface NonValue extends Data permits Clock, DataAccess, Wire {
    interface Factory<T extends NonValue> extends Entity {
        T create(Identifier id);
    }
}
