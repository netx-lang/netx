package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public final class VModule {
    private final Identifier id;
    private final int defIndex;

    private final Map<Integer, Assign> assignments = new HashMap<>();
    private final Map<Integer, Instantiation> instantiations = new HashMap<>();
    private final Map<String, Wire> wires = new HashMap<>();
    private final List<Register> registers = new LinkedList<>();
    private List<DataRef> inputs, outputs;

    public VModule(Identifier id, int defIndex) {
        this.id = id;
        this.defIndex = defIndex;
    }

    public static VModule of(Identifier id, int defIndex) {
        return new VModule(id, defIndex);
    }

    public void add(Wire wire) {
        wires.put(wire.id().name(), wire);
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

    public Collection<Instantiation> getInstantiations() {
        return instantiations.values();
    }

    public Collection<Assign> getAssigns() {
        return assignments.values();
    }

    public Collection<Register> getAllRegisters() {
        return registers;
    }

    public Identifier getId() {
        return id;
    }

    public int getDefIndex() {
        return defIndex;
    }

    public Wire getWire(String name) {
        return wires.get(name);
    }

    public Collection<Wire> getAllWires() {
        return wires.values();
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
