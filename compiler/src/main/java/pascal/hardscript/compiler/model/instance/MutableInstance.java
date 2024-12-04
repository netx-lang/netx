package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.lang.Identifier;

public sealed interface MutableInstance extends Instance permits Data, DataAccess, DataComposition {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends MutableInstance> implements Entity {
        protected final GlobalScope global;

        protected Factory(GlobalScope global) {
            this.global = global;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public InstanceType getType();
    }
}
