package pascal.hardscript.hson.simulator.model;

public final class Register {
    private final DataNode curr, next;
    private final ClockNode clock;
    private final boolean isPosTrigger;

    public Register(DataNode curr, DataNode next, ClockNode clock, boolean isPosTrigger) {
        this.curr = curr;
        this.next = next;
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }


    public static Register of(DataNode state, DataNode next, ClockNode clock, boolean isPosTrigger) {
        return new Register(state, next, clock, isPosTrigger);
    }

    public DataNode getNext() {
        return next;
    }

    public DataNode getCurr() {
        return curr;
    }

    public ClockNode getClock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }
}
