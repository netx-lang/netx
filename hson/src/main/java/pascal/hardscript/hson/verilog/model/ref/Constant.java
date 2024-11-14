package pascal.hardscript.hson.verilog.model.ref;

import pascal.hardscript.util.lang.BitVector;

public record Constant(BitVector value) implements DataRef {

    public static Constant of(BitVector value) {
        return new Constant(value);
    }

}
