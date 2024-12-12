package pascal.hardscript.verilog.model.ref;

import pascal.hardscript.verilog.model.Instantiation;

public record WireRef(Instantiation instance, String name) implements DataRef {
    public static WireRef of(Instantiation instance, String name) {
        return new WireRef(instance, name);
    }
}
