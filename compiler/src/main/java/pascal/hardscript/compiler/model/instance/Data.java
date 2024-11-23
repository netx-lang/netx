package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.Objects;
import java.util.function.Supplier;

public sealed abstract class Data implements MutableInstance permits Clock, Wire {
    protected final Identifier id;
    protected final ComponentScope parent;
    private final int index;

    protected Data(Identifier id, ComponentScope parent, int index) {
        this.parent = parent;
        this.index = index;
        this.id = id;
    }

    public Identifier getIdentifier() {
        return id;
    }

    public SourceLoc getSourceLoc() {
        return getIdentifier().getSourceLoc();
    }

    public Integer getIndex() {
        return index;
    }

    public ComponentScope getParent() {
        return parent;
    }

    @Override
    public boolean isBuiltin() {
        return getIdentifier().name().startsWith("$");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Data data = (Data) o;
        return index == data.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    public abstract static class Factory<T extends Data> implements Entity {
        protected final ComponentScope parent;
        protected final Supplier<Integer> indexer;

        protected Factory(ComponentScope parent, Supplier<Integer> indexer) {
            this.parent = parent;
            this.indexer = indexer;
        }

        public abstract T create(Identifier id);
    }
}
