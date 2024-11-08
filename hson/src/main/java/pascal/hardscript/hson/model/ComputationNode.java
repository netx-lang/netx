package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record ComputationNode(Identifier id) implements Node, RelationNode {

    public static ComputationNode of(Identifier id) {
        return new ComputationNode(id);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
