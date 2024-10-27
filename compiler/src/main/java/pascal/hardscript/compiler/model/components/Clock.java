package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;

public record Clock(Identifier id) implements Component {

    public static class Factory implements Component.Factory<Clock> {

        public static Factory of() {
            return new Factory();
        }

        @Override
        public Clock create(Identifier id) {
            return new Clock(id);
        }
    }
}
