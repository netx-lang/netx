package pascal.hardscript.util.lang;

import java.math.BigInteger;
import java.util.Arrays;

public final class BitVector {

    private final byte[] data;

    private final int size;

    public BitVector(int size) {
        this.size = size;
        this.data = new byte[(size + 7) / 8];
    }

    public BitVector(int size, byte[] array) {
        this.size = size;
        this.data = new byte[(size + 7) / 8];
        int length = Math.min(data.length, array.length);
        System.arraycopy(array, array.length - length, data, data.length - length, length);
        normalize();
    }

    public static BitVector parse(String rep, int size, int radix) {
        var bigInteger = new BigInteger(rep, radix);
        return fromBigInteger(size, bigInteger);
    }

    public static BitVector fromInt(int value) {
        return fromInt(value, Integer.SIZE);
    }

    public static BitVector fromInt(int value, int size) {
        return fromBigInteger(size, BigInteger.valueOf(value));
    }

    public static BitVector fromBoolean(boolean value) {
        return fromInt(value ? 1 : 0, 1);
    }

    public static BitVector fromBigInteger(int size, BigInteger value) {
        return new BitVector(size, value.toByteArray());
    }

    public void set(int index) {
        data[index >> 3] |= (byte) (1 << (index & 7));
    }

    public void clear(int index) {
        data[index >> 3] &= (byte) ~(1 << (index & 7));
    }

    public boolean get(int index) {
        return (data[index >> 3] & (1 << (index & 7))) != 0;
    }

    public boolean msb() {
        return get(size - 1);
    }

    public boolean lsb() {
        return get(0);
    }

    public BitVector ext(int width) {
        if (msb()) {
            var high = BigInteger.ONE.shiftLeft(width - size + 1).subtract(BigInteger.ONE);
            return fromBigInteger(width - size, high).concat(this);
        } else {
            return new BitVector(width, data);
        }
    }

    public BitVector truncate(int width) {
        return new BitVector(width, data);
    }

    public BitVector select(int msb, int lsb) {
        if (msb < lsb) {
            throw new IllegalArgumentException("Invalid range");
        }
        int size = msb - lsb + 1;
        BitVector result = new BitVector(size);
        for (int i = 0; i < size; i++) {
            if (get(lsb + i)) {
                result.set(i);
            }
        }
        return result;
    }

    public BitVector set(int msb, int lsb, BitVector vector) {
        if (msb < lsb) {
            throw new IllegalArgumentException("Invalid range");
        }
        int size = msb - lsb + 1;
        if (size != vector.size) {
            throw new IllegalArgumentException("Invalid vector size");
        }
        for (int i = 0; i < size; i++) {
            if (vector.get(i)) {
                set(lsb + i);
            } else {
                clear(lsb + i);
            }
        }
        return this;
    }

    public int size() {
        return size;
    }

    public void clear() {
        setAllBytes((byte) 0);
    }

    private void setAllBytes(byte value) {
        Arrays.fill(data, value);
    }

    public BitVector copyOf() {
        var result = new BitVector(size);
        System.arraycopy(data, 0, result.data, 0, data.length);
        return result;
    }

    public BigInteger toBigInteger() {
        normalize();
        return new BigInteger(data);
    }

    public BigInteger toUnsignedBigInteger() {
        normalize();
        return new BigInteger(1, data);
    }

    public int toInt() {
        return toBigInteger().intValue();
    }

    public boolean toBoolean() {
        return get(0);
    }

    public BitVector concat(BitVector that) {
        var rep = this.toUnsignedBigInteger().shiftLeft(that.size).or(that.toUnsignedBigInteger());
        return fromBigInteger(this.size + that.size, rep);

    }

    public BitVector neg() {
        return fromBigInteger(size, toUnsignedBigInteger().negate());
    }

    public BitVector not() {
        return fromBigInteger(size, toUnsignedBigInteger().not());
    }

    public BitVector add(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().add(that.toUnsignedBigInteger()));
    }

    public BitVector sub(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().subtract(that.toUnsignedBigInteger()));
    }

    public BitVector mul(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().multiply(that.toUnsignedBigInteger()));
    }

    public BitVector div(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().divide(that.toUnsignedBigInteger()));
    }

    public BitVector mod(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().mod(that.toUnsignedBigInteger()));
    }

    public BitVector pow(BitVector that) {
        return fromBigInteger(size, toUnsignedBigInteger().pow(that.toInt()));
    }

    public BitVector and(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().and(that.toUnsignedBigInteger()));
    }

    public BitVector nand(BitVector that) {
        return this.and(that).not();
    }

    public BitVector or(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().or(that.toUnsignedBigInteger()));
    }

    public BitVector nor(BitVector that) {
        return this.or(that).not();
    }

    public BitVector xor(BitVector that) {
        requiresSameSize(this, that);
        return fromBigInteger(size, toUnsignedBigInteger().xor(that.toUnsignedBigInteger()));
    }

    public BitVector xnor(BitVector that) {
        requiresSameSize(this, that);
        return xor(that).not();
    }

    public BitVector ashr(int offset) {
        if (!msb()) {
            return lshr(offset);
        } else {
            var rep = toUnsignedBigInteger();
            var high = BigInteger.ONE.shiftLeft(size + 1).subtract(BigInteger.ONE);
            var low = BigInteger.ONE.shiftLeft(size - offset).subtract(BigInteger.ONE);
            return fromBigInteger(size, rep.shiftRight(offset).or(high.xor(low)));
        }
    }

    public BitVector lshr(int offset) {
        return fromBigInteger(size, toUnsignedBigInteger().shiftRight(offset));
    }

    public BitVector shl(int offset) {
        return fromBigInteger(size, toUnsignedBigInteger().shiftLeft(offset));
    }

    public boolean eq(BitVector that) {
        return toUnsignedBigInteger().equals(that.toUnsignedBigInteger());
    }

    public boolean ne(BitVector that) {
        return !eq(that);
    }

    public boolean sgt(BitVector that) {
        return toBigInteger().compareTo(that.toBigInteger()) > 0;
    }

    public boolean sge(BitVector that) {
        return toBigInteger().compareTo(that.toBigInteger()) >= 0;
    }

    public boolean slt(BitVector that) {
        return toBigInteger().compareTo(that.toBigInteger()) < 0;
    }

    public boolean sle(BitVector that) {
        return toBigInteger().compareTo(that.toBigInteger()) <= 0;
    }

    public boolean ugt(BitVector that) {
        return toUnsignedBigInteger().compareTo(that.toUnsignedBigInteger()) > 0;
    }

    public boolean uge(BitVector that) {
        return toUnsignedBigInteger().compareTo(that.toUnsignedBigInteger()) >= 0;
    }

    public boolean ult(BitVector that) {
        return toUnsignedBigInteger().compareTo(that.toUnsignedBigInteger()) < 0;
    }

    public boolean ule(BitVector that) {
        return toUnsignedBigInteger().compareTo(that.toUnsignedBigInteger()) <= 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        return eq((BitVector) obj);
    }

    private void normalize() {
        int offset = 8 - ((data.length << 3) - size);
        var mask = (byte) ((1 << offset) - 1);
        data[0] &= mask;
    }

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    @Override
    public String toString() {
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        return size + "'h" + new String(hexChars);
    }

    private static void requiresSameSize(BitVector a, BitVector b) {
        if (a.size() != b.size()) {
            throw new IllegalArgumentException("Bit vectors must have the same size");
        }
    }
}
