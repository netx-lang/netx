package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import java.util.Objects;

public sealed class Wire implements Data permits Clock {
    protected final ComponentScope parent;
    protected final Identifier id;
    private final int index;
    private Type type;

    public Wire(Identifier id, ComponentScope parent, Type type, int index) {
        this.parent = parent;
        this.id = id;
        this.index = index;
        this.type = type;
    }

    public static Wire of(Identifier id, ComponentScope parent, Type width, int index) {
        return new Wire(id, parent, width, index);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return id.toStringWithLoc();
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
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
        Wire wire = (Wire) o;
        return index == wire.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    public static class Factory extends Data.Factory<Wire> {
        private final Type type;

        public Factory(Type type, NXCompiler.WireIndexer indexer) {
            super(indexer);
            if (type instanceof BitType(int width) && width == 0) {
                Logger.error("Cannot create a wire with zero width");
                throw new CompileCancellationException();
            }
            this.type = type;
        }

        @Override
        public Wire create(ComponentScope parent, Identifier id) {
            return new Wire(id, parent, type, indexer.next());
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
