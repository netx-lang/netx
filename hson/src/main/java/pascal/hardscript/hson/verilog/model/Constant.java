package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.util.lang.BitVector;

public record Constant(BitVector value) implements Data {

    public static Constant of(BitVector value) {
        return new Constant(value);
    }

}
