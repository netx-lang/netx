package pascal.hardscript.compiler.model.value;

import pascal.hardscript.util.lang.BitVector;

public record Int32Value(int value) implements Value, IntegerValue {
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
    public IntegerValue pow(IntegerValue other) {
        return Int32Value.of((int) Math.pow(value, other.toInt()));
    }

    @Override
    public IntegerValue and(IntegerValue other) {
        return Int32Value.of(value & other.toInt());
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
    public Int32Value neg() {
        return Int32Value.of(-value);
    }

    @Override
    public Int32Value bitwiseNot() {
        return Int32Value.of(~value);
    }
}
