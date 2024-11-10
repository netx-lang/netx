package pascal.hardscript.hson.verilog.model;

public record Register(Wire curr, Wire next, Wire clock, boolean isPosTrigger) {
    public static Register of(Wire curr, Wire next, Wire clock, boolean isPosTrigger) {
        return new Register(curr, next, clock, isPosTrigger);
    }
}
