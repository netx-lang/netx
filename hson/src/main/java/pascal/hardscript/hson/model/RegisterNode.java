package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record RegisterNode(Identifier id, ClockNode clock, boolean isPosTrigger) implements StreamNode, RelationNode {
    public static RegisterNode of(Identifier id, ClockNode clock, boolean isPosTrigger) {
        return new RegisterNode(id, clock, isPosTrigger);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
