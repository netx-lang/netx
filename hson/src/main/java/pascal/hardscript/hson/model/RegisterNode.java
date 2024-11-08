package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class RegisterNode extends DataNode implements StreamNode, RelationNode {
    private final Identifier id;
    private final ClockNode clock;
    private final boolean isPosTrigger;

    public RegisterNode(Identifier id, ClockNode clock, boolean isPosTrigger) {
        this.id = id;
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }

    public static RegisterNode of(Identifier id, ClockNode clock, boolean isPosTrigger) {
        return new RegisterNode(id, clock, isPosTrigger);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public ClockNode getClock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }
}
