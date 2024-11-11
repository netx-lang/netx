package pascal.hardscript.hson.verilog.model;

import java.util.ArrayList;
import java.util.List;

public final class VModule {
    private final String origin;

    private final VModule parent;
    private final List<VModule> children = new ArrayList<>();

    private List<Data> inputs;
    private List<Data> outputs;
    private final List<Wire> wires = new ArrayList<>();
    private final List<Assign> assign = new ArrayList<>();
    private final List<Register> registers = new ArrayList<>();

    public VModule(String origin, VModule parent) {
        this.origin = origin;
        this.parent = parent;
    }

    public static VModule of(String id, VModule parent) {
        return new VModule(id, parent);
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
