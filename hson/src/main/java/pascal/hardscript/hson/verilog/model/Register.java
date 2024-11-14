package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;

public record Register(DataRef in, DataRef out, DataRef clock, boolean isPosTrigger) {
    public static Register of(DataRef in, DataRef out, DataRef clock, boolean isPosTrigger) {
        return new Register(in, out, clock, isPosTrigger);
    }
}
