package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.Scope;

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
    public String toString() {
        return id.toString();
    }

    public static class Factory implements Data.Factory<Wire> {
        private final InstanceType type;
        private final Scope parent;

        private Factory(Scope parent, InstanceType type) {
            this.type = type;
            this.parent = parent;
        }

        public static Factory of(Scope parent, InstanceType type) {
            return new Factory(parent, type);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, parent, type);
        }
    }
}
