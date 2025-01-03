package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.GlobalScope;
import pascal.netx.util.lang.Identifier;

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
