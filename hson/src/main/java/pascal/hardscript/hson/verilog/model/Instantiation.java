package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;

import java.util.List;

public final class Instantiation {
    private final int instIndex;
    private final VModule module;

    private List<DataRef> inputs;
    private List<DataRef> outputs;

    public Instantiation(VModule module, int instIndex) {
        this.module = module;
        this.instIndex = instIndex;
    }

    public static Instantiation of(VModule module, int instIndex) {
        return new Instantiation(module, instIndex);
    }

    public VModule getModule() {
        return module;
    }

    public int getInstIndex() {
        return instIndex;
    }

    public List<DataRef> getInputs() {
        return inputs;
    }

    public void setInputs(List<DataRef> inputs) {
        this.inputs = inputs;
    }

    public List<DataRef> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<DataRef> outputs) {
        this.outputs = outputs;
    }
}
