package pascal.hardscript.hson.verilog.model;

public record Register(Data in, Data out, Data clock, boolean isPosTrigger) {
    public static Register of(Data in, Data out, Data clock, boolean isPosTrigger) {
        return new Register(in, out, clock, isPosTrigger);
    }
}
