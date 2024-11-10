package pascal.hardscript.hson.verilog.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class VModule {
    private final String origin;
    private final List<Wire> wires = new ArrayList<>();
    private final List<VModule> children = new ArrayList<>();
    private final Map<Wire, List<Register>> registers = new HashMap<>();

    public VModule(String origin) {
        this.origin = origin;
    }

    public static VModule of(String id) {
        return new VModule(id);
    }

    public void addWire(Wire wire) {
        wires.add(wire);
    }

    public List<Wire> getWires() {
        return wires;
    }

    public void addRegister(Wire clock, Register register) {
        registers.computeIfAbsent(clock, _ -> new ArrayList<>()).add(register);
    }

    public Map<Wire, List<Register>> getRegisters() {
        return registers;
    }

    public void addSubModule(VModule module) {
        children.add(module);
    }

    public List<VModule> getChildren() {
        return children;
    }

}
