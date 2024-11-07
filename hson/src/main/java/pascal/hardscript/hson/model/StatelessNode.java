package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record StatelessNode(Identifier id, int width) implements Node {
    public static StatelessNode of(Identifier identifier, int width) {
        return new StatelessNode(identifier, width);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
