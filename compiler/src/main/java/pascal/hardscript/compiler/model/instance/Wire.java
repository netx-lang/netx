package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

import java.util.Objects;

public final class Wire implements Data {
    private final Identifier id;
    private final Scope parent;
    private InstanceType type;

    public Wire(Identifier id, Scope parent, InstanceType type) {
        this.id = id;
        this.parent = parent;
        this.type = type;
    }

    public static Wire of(Identifier id, Scope parent, InstanceType width) {
        return new Wire(id, parent, width);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public Scope getParent() {
        return parent;
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

    public static class Factory implements Data.Factory<Wire> {
        private final InstanceType type;
        private final ComponentScope parent;

        private Factory(ComponentScope parent, InstanceType type) {
            this.type = type;
            this.parent = parent;
        }

        public static Factory of(ComponentScope parent, InstanceType type) {
            return new Factory(parent, type);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, parent, type);
        }

        @Override
        public ComponentScope getParent() {
            return parent;
        }
    }
}
