package pascal.hardscript.compiler.model.value;

import pascal.hardscript.util.lang.BitVector;

public record BitVectorValue(BitVector value) implements Value {

    public static BitVectorValue of(BitVector value) {
        return new BitVectorValue(value);
    }
}
