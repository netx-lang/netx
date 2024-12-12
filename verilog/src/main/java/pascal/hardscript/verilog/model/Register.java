package pascal.hardscript.verilog.model;

import pascal.hardscript.verilog.model.ref.DataRef;

import java.util.function.Function;

public final class Register {
    private final boolean isPosTrigger;
    private DataRef in;
    private DataRef out;
    private DataRef clock;
    private Wire trueReg;

    public Register(boolean isPosTrigger) {
        this.isPosTrigger = isPosTrigger;
    }

    public static Register of(boolean isPosTrigger) {
        return new Register(isPosTrigger);
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

    public void setClock(DataRef clock) {
        this.clock = clock;
    }

    public void setOut(DataRef out) {
        this.out = out;
    }

    public void setIn(DataRef in) {
        this.in = in;
    }

    public void resetRef(Function<DataRef, DataRef> mapper) {
        in = mapper.apply(in);
        out = mapper.apply(out);
        clock = mapper.apply(clock);
    }
}
