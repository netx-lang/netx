package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.verilog.model.ref.DataRef;
import pascal.hardscript.util.lang.BitVector;

public record Constant(BitVector value) implements DataRef {

    public static Constant of(BitVector value) {
        return new Constant(value);
    }

}
