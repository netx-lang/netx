package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;

public final class Wire implements NonValue {
    private final Identifier id;
    private final Scope parent;
    private DataType type;

    public Wire(Identifier id, Scope parent, DataType type) {
        this.id = id;
        this.parent = parent;
        this.type = type;
    }

    public static Wire of(Identifier id, Scope parent, DataType width) {
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
    public DataType getType() {
        return type;
    }

    @Override
    public void setType(DataType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static class Factory implements NonValue.Factory<Wire> {
        private final DataType type;
        private final Scope parent;

        private Factory(Scope parent, DataType type) {
            this.type = type;
            this.parent = parent;
        }

        public static Factory of(Scope parent, DataType type) {
            return new Factory(parent, type);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, parent, type);
        }
    }
}
