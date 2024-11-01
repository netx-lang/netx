package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;

public record Wire(Identifier id, Scope parent, DataType type) implements Data {

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

    public static class Factory implements Data.Factory<Wire> {
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
