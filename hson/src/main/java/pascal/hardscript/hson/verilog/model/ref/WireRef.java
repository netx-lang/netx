package pascal.hardscript.hson.verilog.model.ref;

import pascal.hardscript.hson.verilog.model.Instantiation;

public record WireRef(Instantiation instance, String name) implements DataRef {
    public static WireRef of(Instantiation instance, String name) {
        return new WireRef(instance, name);
    }
}
