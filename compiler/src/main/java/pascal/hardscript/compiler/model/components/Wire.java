package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;

public record Wire(Identifier id, Scope parent, int width) implements Component {

    public static Wire of(Identifier id, Scope parent, int width) {
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

    public static class Factory implements Component.Factory<Wire> {
        private final int width;
        private final Scope parent;

        private Factory(Scope parent, int width) {
            this.width = width;
            this.parent = parent;
        }

        public static Factory of(Scope parent, int width) {
            return new Factory(parent, width);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, parent, width);
        }
    }
}
