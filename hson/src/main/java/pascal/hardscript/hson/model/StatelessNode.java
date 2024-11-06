package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record StatelessNode(Identifier identifier, int width) implements Node {
    public static StatelessNode of(Identifier identifier, int width) {
        return new StatelessNode(identifier, width);
    }
}
