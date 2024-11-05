package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.util.lang.BitVector;

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
    public BooleanValue gt(IntegerValue other) {
        return BooleanValue.of(value > other.toInt());
    }

    @Override
    public BooleanValue ge(IntegerValue other) {
        return BooleanValue.of(value >= other.toInt());
    }

    @Override
    public BooleanValue lt(IntegerValue other) {
        return BooleanValue.of(value < other.toInt());
    }

    @Override
    public BooleanValue le(IntegerValue other) {
        return BooleanValue.of(value <= other.toInt());
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
    public InstanceType getType() {
        return BitType.forInt32();
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }
}
