package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;

import java.util.function.Function;

public final class Register {
    private final boolean isPosTrigger;
    private DataRef in;
    private DataRef out;
    private DataRef clock;
    private Wire trueReg;

    public Register(DataRef in, DataRef out, DataRef clock, boolean isPosTrigger) {
        this.in = in;
        this.out = out;
        this.clock = clock;
        this.isPosTrigger = isPosTrigger;
    }

    public static Register of(DataRef in, DataRef out, DataRef clock, boolean isPosTrigger) {
        return new Register(in, out, clock, isPosTrigger);
    }

    public DataRef in() {
        return in;
    }

    public DataRef out() {
        return out;
    }

    public DataRef clock() {
        return clock;
    }

    public boolean isPosTrigger() {
        return isPosTrigger;
    }

    public Wire getTrueReg() {
        return trueReg;
    }

    public void setTrueReg(Wire trueReg) {
        this.trueReg = trueReg;
    }

    public void resetRef(Function<DataRef, DataRef> mapper) {
        in = mapper.apply(in);
        out = mapper.apply(out);
        clock = mapper.apply(clock);
    }
}
