package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;

public record Clock(Identifier id, Scope parent) implements Component {

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    public static class Factory implements Component.Factory<Clock> {
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
