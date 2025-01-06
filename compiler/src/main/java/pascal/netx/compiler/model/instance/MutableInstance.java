package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.util.lang.Identifier;

public sealed interface MutableInstance extends Instance permits Data, DataAccess, DataComposition {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends MutableInstance> implements Entity {
        protected final NXCompiler compiler;

        protected Factory(NXCompiler compiler) {
            this.compiler = compiler;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public InstanceType getType();
    }
}
