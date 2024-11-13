package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

import java.util.Objects;
import java.util.function.Supplier;

public final class Wire extends Data {
    private final Scope parent;
    private InstanceType type;

    public Wire(Identifier id, Scope parent, InstanceType type, int index) {
        super(id, parent, index);
        this.parent = parent;
        this.type = type;
    }

    public static Wire of(Identifier id, Scope parent, InstanceType width, int index) {
        return new Wire(id, parent, width, index);
    }

    @Override
    public InstanceType getType() {
        return type;
    }

    @Override
    public void setType(InstanceType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Wire wire = (Wire) o;
        return Objects.equals(id, wire.id) && Objects.equals(parent, wire.parent) && Objects.equals(type, wire.type);
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static class Factory extends Data.Factory<Wire> {
        private final InstanceType type;

        private Factory(Supplier<Integer> indexer, ComponentScope parent, InstanceType type) {
            super(indexer, parent);
            this.type = type;
        }

        public static Factory of(Supplier<Integer> indexer, ComponentScope parent, InstanceType type) {
            return new Factory(indexer, parent, type);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, parent, type, indexer.get());
        }
    }
}
