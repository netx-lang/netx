package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

import java.util.function.Supplier;

public sealed abstract class Data implements Instance permits Clock, DataAccess, Wire {
    private final int index;
    protected final Identifier id;
    protected final Scope parent;

    protected Data(Identifier id, Scope parent, int index) {
        this.parent = parent;
        this.index = index;
        this.id = id;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public Integer getIndex() {
        return index;
    }

    public abstract static class Factory<T extends Data> implements Entity {
        protected final Supplier<Integer> indexer;
        protected final ComponentScope parent;

        protected Factory(Supplier<Integer> indexer, ComponentScope parent) {
            this.indexer = indexer;
            this.parent = parent;
        }

        public abstract T create(Identifier id);

        public ComponentScope getParent() {
            return parent;
        }
    }
}
