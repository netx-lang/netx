package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;

public record Register(Identifier id, int width) implements Component {

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public static class Factory implements Component.Factory<Register> {
        private final int width;

        private Factory(int width) {
            this.width = width;
        }

        public static Factory of(int width) {
            return new Factory(width);
        }

        @Override
        public Register create(Identifier id) {
            return new Register(id, width);
        }
    }
}
