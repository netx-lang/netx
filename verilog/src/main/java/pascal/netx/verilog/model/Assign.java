package pascal.netx.verilog.model;

import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.verilog.model.ref.DataRef;

import java.util.ArrayList;
import java.util.List;

public final class Assign {
    private final BuiltinComponent builtin;
    private List<DataRef> inputs;
    private List<DataRef> outputs;

    public Assign(BuiltinComponent builtin) {
        this.builtin = builtin;
    }

    public static Assign of(BuiltinComponent builtin) {
        return new Assign(builtin);
    }

    public BuiltinComponent builtin() {
        return builtin;
    }

    public List<DataRef> inputs() {
        return inputs;
    }

    public List<DataRef> outputs() {
        return outputs;
    }

    public void setInputs(List<DataRef> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public void setOutputs(List<DataRef> outputs) {
        this.outputs = new ArrayList<>(outputs);
    }
}
