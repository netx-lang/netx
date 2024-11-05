package pascal.hardscript.compiler.model.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.util.lang.BitVector;

public record BitVectorValue(BitVector value) implements IntegerValue {

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
    public IntegerValue mul(IntegerValue other) {
        return BitVectorValue.of(value.mul(other.toBitVector()));
    }

    @Override
    public IntegerValue div(IntegerValue other) {
        return BitVectorValue.of(value.div(other.toBitVector()));
    }

    @Override
    public IntegerValue mod(IntegerValue other) {
        return BitVectorValue.of(value.mod(other.toBitVector()));
    }

    @Override
    public IntegerValue and(IntegerValue other) {
        return BitVectorValue.of(value.and(other.toBitVector()));
    }

    @Override
    public IntegerValue or(IntegerValue other) {
        return BitVectorValue.of(value.or(other.toBitVector()));
    }

    @Override
    public IntegerValue xor(IntegerValue other) {
        return BitVectorValue.of(value.xor(other.toBitVector()));
    }

    @Override
    public IntegerValue xnor(IntegerValue other) {
        return BitVectorValue.of(value.xnor(other.toBitVector()));
    }

    @Override
    public IntegerValue ashr(IntegerValue other) {
        return BitVectorValue.of(value.ashr(other.toInt()));
    }

    @Override
    public IntegerValue lshr(IntegerValue other) {
        return BitVectorValue.of(value.lshr(other.toInt()));
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
    public BooleanValue eq(IntegerValue other) {
        return BooleanValue.of(value.eq(other.toBitVector()));
    }

    @Override
    public BooleanValue ne(IntegerValue other) {
        return BooleanValue.of(value.ne(other.toBitVector()));
    }

    @Override
    public BooleanValue gt(IntegerValue other) {
        return BooleanValue.of(value.gt(other.toBitVector()));
    }

    @Override
    public BooleanValue ge(IntegerValue other) {
        return BooleanValue.of(value.ge(other.toBitVector()));
    }

    @Override
    public BooleanValue lt(IntegerValue other) {
        return BooleanValue.of(value.lt(other.toBitVector()));
    }

    @Override
    public BooleanValue le(IntegerValue other) {
        return BooleanValue.of(value.le(other.toBitVector()));
    }

    @Override
    public BitVectorValue neg() {
        return BitVectorValue.of(value.negative());
    }

    @Override
    public IntegerValue bitwiseNot() {
        return BitVectorValue.of(value.invert());
    }

    @Override
    public DataType getType() {
        return BitType.of(value.size());
    }
}
