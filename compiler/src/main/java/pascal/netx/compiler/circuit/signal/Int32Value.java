package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.circuit.signal.type.BitType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.util.lang.BitVector;

import javax.annotation.Nonnull;

public record Int32Value(int value) implements IntegerValue {
    public static Int32Value of(int value) {
        return new Int32Value(value);
    }

    @Override
    public int toInt() {
        return value;
    }

    @Override
    public BitVector toBitVector() {
        return BitVector.fromInt(value);
    }

    @Override
    public Int32Value add(IntegerValue other) {
        return Int32Value.of(value + other.toInt());
    }

    @Override
    public Int32Value sub(IntegerValue other) {
        return Int32Value.of(value - other.toInt());
    }

    @Override
    public IntegerValue mul(IntegerValue other) {
        return Int32Value.of(value * other.toInt());
    }

    @Override
    public IntegerValue div(IntegerValue other) {
        return Int32Value.of(value / other.toInt());
    }

    @Override
    public IntegerValue mod(IntegerValue other) {
        return Int32Value.of(value % other.toInt());
    }

    @Override
    public IntegerValue pow(IntegerValue other) {
        return Int32Value.of((int) Math.pow(value, other.toInt()));
    }

    @Override
    public IntegerValue and(IntegerValue other) {
        return Int32Value.of(value & other.toInt());
    }

    @Override
    public IntegerValue or(IntegerValue other) {
        return Int32Value.of(value | other.toInt());
    }

    @Override
    public IntegerValue xor(IntegerValue other) {
        return Int32Value.of(value ^ other.toInt());
    }

    @Override
    public IntegerValue xnor(IntegerValue other) {
        return Int32Value.of(~(value ^ other.toInt()));
    }

    @Override
    public IntegerValue ashr(IntegerValue other) {
        return Int32Value.of(value >> other.toInt());
    }

    @Override
    public IntegerValue lshr(IntegerValue other) {
        return Int32Value.of(value >>> other.toInt());
    }

    @Override
    public IntegerValue shl(IntegerValue other) {
        return Int32Value.of(value << other.toInt());
    }

    @Override
    public BooleanValue eq(IntegerValue other) {
        return BooleanValue.of(value == other.toInt());
    }

    @Override
    public BooleanValue ne(IntegerValue other) {
        return BooleanValue.of(value != other.toInt());
    }

    @Override
    public BooleanValue sgt(IntegerValue other) {
        return BooleanValue.of(value > other.toInt());
    }

    @Override
    public BooleanValue sge(IntegerValue other) {
        return BooleanValue.of(value >= other.toInt());
    }

    @Override
    public BooleanValue slt(IntegerValue other) {
        return BooleanValue.of(value < other.toInt());
    }

    @Override
    public BooleanValue sle(IntegerValue other) {
        return BooleanValue.of(value <= other.toInt());
    }

    @Override
    public BooleanValue ugt(IntegerValue other) {
        return BooleanValue.of(Integer.compareUnsigned(value, other.toInt()) > 0);
    }

    @Override
    public BooleanValue uge(IntegerValue other) {
        return BooleanValue.of(Integer.compareUnsigned(value, other.toInt()) >= 0);
    }

    @Override
    public BooleanValue ult(IntegerValue other) {
        return BooleanValue.of(Integer.compareUnsigned(value, other.toInt()) < 0);
    }

    @Override
    public BooleanValue ule(IntegerValue other) {
        return BooleanValue.of(Integer.compareUnsigned(value, other.toInt()) <= 0);
    }

    @Override
    public Int32Value neg() {
        return Int32Value.of(-value);
    }

    @Override
    public Int32Value bitwiseNot() {
        return Int32Value.of(~value);
    }

    @Override
    public Type getType() {
        return BitType.forInt32();
    }

    @Override
    @Nonnull
    public String toString() {
        return String.valueOf(value);
    }
}
