package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.ModuleScope;
import pascal.netx.util.lang.Identifier;

public sealed interface MutableInstance extends Instance permits Data, DataAccess, DataComposition {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends MutableInstance> implements Entity {
        protected final ModuleScope module;

        protected Factory(ModuleScope module) {
            this.module = module;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public InstanceType getType();
    }
}
