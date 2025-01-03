package pascal.netx.verilog.model.ref;

import pascal.netx.util.lang.BitVector;

public record Constant(BitVector value) implements DataRef {

    public static Constant of(BitVector value) {
        return new Constant(value);
    }

}
