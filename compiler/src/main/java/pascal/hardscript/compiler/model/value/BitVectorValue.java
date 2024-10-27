package pascal.hardscript.compiler.model.value;

import pascal.hardscript.util.lang.BitVector;

public record BitVectorValue(BitVector value) implements Value, IntegerValue {

    public static BitVectorValue of(BitVector value) {
        return new BitVectorValue(value);
    }

    @Override
    public int toInt() {
        return value.toInt();
    }

    @Override
    public BitVectorValue negative() {
        return BitVectorValue.of(value.negative());
    }

    @Override
    public IntegerValue bitwiseNot() {
        return BitVectorValue.of(value.bitwiseNot());
    }
}
