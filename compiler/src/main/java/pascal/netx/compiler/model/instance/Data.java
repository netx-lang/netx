package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.Objects;

public sealed abstract class Data implements MutableInstance permits Clock, Wire {
    protected final ComponentScope parent;
    protected final Identifier id;
    private final int index;

    protected Data(Identifier id, ComponentScope parent, int index) {
        this.parent = parent;
        this.index = index;
        this.id = id;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public SourceLoc getSourceLoc() {
        return getIdentifier().getSourceLoc();
    }

    @Override
    public int getIndex() {
        return index;
    }

    @Override
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

    public abstract static class Factory<T extends Data> extends MutableInstance.Factory<T> {

        protected Factory(PackageScope module) {
            super(module);
        }

        public abstract T create(ComponentScope parent, Identifier id);
    }
}
