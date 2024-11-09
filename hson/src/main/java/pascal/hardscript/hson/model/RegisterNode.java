package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class RegisterNode extends Node {
    private static final Identifier IDENTIFIER = Identifier.of("REGISTER");

    private final DataNode state, next;
    private final ClockNode clock;
    private final boolean isPosTrigger;

    public RegisterNode(DataNode state, DataNode next, ClockNode clock, boolean isPosTrigger) {
        super(IDENTIFIER);
        this.state = state;
        this.next = next;
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }


    public static RegisterNode of(DataNode state, DataNode next, ClockNode clock, boolean isPosTrigger) {
        return new RegisterNode(state, next, clock, isPosTrigger);
    }

    public DataNode getState() {
        return state;
    }

    public DataNode getNext() {
        return next;
    }

    public ClockNode getClock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }
}
