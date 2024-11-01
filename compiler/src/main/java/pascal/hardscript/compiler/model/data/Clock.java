package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;

public record Clock(Identifier id, Scope parent) implements Data {

    private static final BitType TYPE = new BitType(1);

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
        return TYPE;
    }

    public static class Factory implements Data.Factory<Clock> {
        private final Scope parent;

        public Factory(Scope parent) {
            this.parent = parent;
        }

        public static Factory of(Scope parent) {
            return new Factory(parent);
        }

        @Override
        public Clock create(Identifier id) {
            return new Clock(id, parent);
        }
    }
}
