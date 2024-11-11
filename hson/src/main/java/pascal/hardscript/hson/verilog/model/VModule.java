package pascal.hardscript.hson.verilog.model;

import java.util.ArrayList;
import java.util.List;

public final class VModule {
    private final String origin;
    private final List<VModule> children = new ArrayList<>();

    private List<Data> inputs, outputs;
    private final List<Wire> wires = new ArrayList<>();
    private final List<Assign> assign = new ArrayList<>();
    private final List<Register> registers = new ArrayList<>();

    public VModule(String origin) {
        this.origin = origin;
    }

    public static VModule of(String id) {
        return new VModule(id);
    }

    public String getOrigin() {
        return origin;
    }

    public void addWire(Wire wire) {
        wires.add(wire);
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

    public void addSubModule(VModule module) {
        children.add(module);
    }

    public List<VModule> getChildren() {
        return children;
    }

    public void addAssign(Assign assignItem) {
        assign.add(assignItem);
    }

    public List<Assign> getAssign() {
        return assign;
    }

    public List<Register> getRegisters() {
        return registers;
    }

    public void addRegister(Register register) {
        registers.add(register);
    }
}
