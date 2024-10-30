package pascal.hardscript.compiler.model.value;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.util.lang.BitVector;

public record BitVectorValue(BitVector value) implements Value, IntegerValue {

    private static final Logger log = LoggerFactory.getLogger(BitVectorValue.class);

    public static BitVectorValue of(BitVector value) {
        return new BitVectorValue(value);
    }

    @Override
    public int toInt() {
        if (value.size() > 32) {
            log.warn("Truncating bit vector value {} to 32 bits", value);
        }
        return value.toInt();
    }

    @Override
    public BitVector toBitVector() {
        return value.copyOf();
    }

    @Override
    public IntegerValue add(IntegerValue other) {
        return BitVectorValue.of(value.add(other.toBitVector()));
    }

    @Override
    public IntegerValue sub(IntegerValue other) {
        return BitVectorValue.of(value.sub(other.toBitVector()));
    }

    @Override
    public IntegerValue and(IntegerValue other) {
        return BitVectorValue.of(value.and(other.toBitVector()));
    }

    @Override
    public IntegerValue pow(IntegerValue other) {
        return BitVectorValue.of(value.pow(other.toBitVector()));
    }

    @Override
    public IntegerValue shl(IntegerValue other) {
        return BitVectorValue.of(value.shl(other.toInt()));
    }

    @Override
    public BitVectorValue neg() {
        return BitVectorValue.of(value.negative());
    }

    @Override
    public IntegerValue bitwiseNot() {
        return BitVectorValue.of(value.bitwiseNot());
    }
}
