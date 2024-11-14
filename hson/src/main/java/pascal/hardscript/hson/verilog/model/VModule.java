package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.util.lang.Identifier;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VModule {
    private final Identifier origin;

    private final Map<Integer, Assign> assignments = new HashMap<>();
    private final Map<Integer, Instantiation> instantiations = new HashMap<>();

    private List<String> inputs, outputs;
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

    public void add(int index, Assign assign) {
        assignments.put(index, assign);
    }

    public void add(Register register) {
        registers.add(register);
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

    public List<String> getInputs() {
        return inputs;
    }

    public void setInputs(List<String> inputs) {
        this.inputs = inputs;
    }

    public List<String> getOutputs() {
        return outputs;
    }

    public void setOutputs(List<String> outputs) {
        this.outputs = outputs;
    }

    public List<Register> getRegisters() {
        return registers;
    }
}
