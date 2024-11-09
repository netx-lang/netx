package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class RegisterNode extends DataNode {
    private static final Identifier IDENTIFIER = Identifier.of("REGISTER");
    private final ClockNode clock;
    private final boolean isPosTrigger;

    public RegisterNode(int width, ClockNode clock, boolean isPosTrigger) {
        super(IDENTIFIER, width);
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }

    public static RegisterNode of(int width, ClockNode clock, boolean isPosTrigger) {
        return new RegisterNode(width, clock, isPosTrigger);
    }

    public ClockNode getClock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }
}
