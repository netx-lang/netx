package pascal.hardscript.compiler.model.meta;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.type.Type;

public class MetaVariable implements Entity {
    private final Type type;

    public MetaVariable(Type type) {
        this.type = type;
    }
}
