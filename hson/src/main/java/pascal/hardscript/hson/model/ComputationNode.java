package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record ComputationNode(Identifier id) implements Node {
    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
