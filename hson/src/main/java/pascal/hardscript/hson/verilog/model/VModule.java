package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.data.Data;
import pascal.hardscript.hson.verilog.model.data.Wire;
import pascal.hardscript.util.lang.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VModule {
    private final Identifier origin;
    private final Map<Integer, Instantiation> instantiations = new HashMap<>();

    private List<Data> inputs, outputs;
    private final List<Wire> wires = new ArrayList<>();
    private final List<Register> registers = new ArrayList<>();

    public VModule(Identifier origin) {
        this.origin = origin;
    }

    public static VModule of(Identifier id) {
        return new VModule(id);
    }

    public void add(Wire wire) {
        wires.add(wire);
    }

    public void add(Instantiation instantiation) {
        instantiations.put(instantiation.getInstIndex(), instantiation);
    }

    public Instantiation getInstantiation(int instIndex) {
        return instantiations.get(instIndex);
    }

    public Identifier getOrigin() {
        return origin;
    }

    public List<Wire> getWires() {
        return wires;
    }

    public List<Data> getInputs() {
        return inputs;
    }

    public void setInputs(List<Data> inputs) {
        this.inputs = inputs;
    }

    public List<Data> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<Data> outputs) {
        this.outputs = outputs;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void addRegister(Register register) {
        registers.add(register);
    }
}
