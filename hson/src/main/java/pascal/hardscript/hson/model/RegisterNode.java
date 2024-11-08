package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class RegisterNode extends DataNode {
    private final ClockNode clock;
    private final boolean isPosTrigger;

    public RegisterNode(Identifier id, int width, ClockNode clock, boolean isPosTrigger) {
        super(id, width);
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }

    public static RegisterNode of(Identifier id, int width, ClockNode clock, boolean isPosTrigger) {
        return new RegisterNode(id, width, clock, isPosTrigger);
    }

    public ClockNode getClock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }
}
