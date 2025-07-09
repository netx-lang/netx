package pascal.netx.compiler.model.variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;

public record BitVectorValue(BitVector value) implements IntegerValue {

    private static final Logger log = LoggerFactory.getLogger(BitVectorValue.class);

    public static BitVectorValue of(BitVector value) {
        return new BitVectorValue(value);
    }

    @Nonnull
    @Override
    public String toString() {
        return value.toString();
    }

    @Override
    public int toInt() {
        if (value.size() > 32) {
            log.warn("Truncating bit vector rhs {} to 32 bits", value);
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
    public BooleanValue sgt(IntegerValue other) {
        return BooleanValue.of(value.sgt(other.toBitVector()));
    }

    @Override
    public BooleanValue sge(IntegerValue other) {
        return BooleanValue.of(value.sge(other.toBitVector()));
    }

    @Override
    public BooleanValue slt(IntegerValue other) {
        return BooleanValue.of(value.slt(other.toBitVector()));
    }

    @Override
    public BooleanValue sle(IntegerValue other) {
        return BooleanValue.of(value.sle(other.toBitVector()));
    }

    @Override
    public BooleanValue ugt(IntegerValue other) {
        return BooleanValue.of(value.ugt(other.toBitVector()));
    }

    @Override
    public BooleanValue uge(IntegerValue other) {
        return BooleanValue.of(value.uge(other.toBitVector()));
    }

    @Override
    public BooleanValue ult(IntegerValue other) {
        return BooleanValue.of(value.ult(other.toBitVector()));
    }

    @Override
    public BooleanValue ule(IntegerValue other) {
        return BooleanValue.of(value.ule(other.toBitVector()));
    }

    @Override
    public BitVectorValue neg() {
        return BitVectorValue.of(value.neg());
    }

    @Override
    public IntegerValue bitwiseNot() {
        return BitVectorValue.of(value.not());
    }

    @Override
    public Type getType() {
        return BitType.of(value.size());
    }
}
