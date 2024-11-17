package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.util.lang.Identifier;

import java.util.*;

public final class VModule {
    private final Identifier id;
    private final int defIndex;

    private final Map<Integer, Assign> assignments = new HashMap<>();
    private final Map<Integer, Instantiation> instantiations = new HashMap<>();

    private List<String> inputs, outputs;
    private final Map<String, Wire> wires = new HashMap<>();
    private final List<Register> registers = new LinkedList<>();

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
}
