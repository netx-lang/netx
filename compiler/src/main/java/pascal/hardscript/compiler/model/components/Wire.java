package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;

public record Wire(Identifier id, int width) implements Component {

    public static Wire of(Identifier id, int width) {
        return new Wire(id, width);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public static class Factory implements Component.Factory<Wire> {
        private final int width;

        private Factory(int width) {
            this.width = width;
        }

        public static Factory of(int width) {
            return new Factory(width);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, width);
        }
    }
}
