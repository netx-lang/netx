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

    private BitVector(int size, byte[] data) {
        this.size = size;
        this.data = new byte[(size + 7) / 8];
        int start = Math.max(0, data.length - this.data.length);
        System.arraycopy(data, start, this.data, 0, Math.min(data.length, this.data.length));
    }

    public static BitVector parse(String rep, int size, int radix) {
        if (radix == 10) {
            BigInteger value = new BigInteger(rep);
            return new BitVector(size, value.toByteArray());
        } else {
            BitVector result = new BitVector(size);
            int valid = Integer.bitCount(radix - 1);
            for (int i = 0; i < rep.length(); i++) {
                int digit = Character.digit(rep.charAt(rep.length() - i - 1), radix);
                for (int j = 0; j < valid; j++) {
                    int index = i * valid + j;
                    if ((digit & (1 << j)) != 0 && index < size) {
                        result.set(index);
                    }
                }
            }
            return result;
        }
    }

    public static BitVector fromInt(int value) {
        return new BitVector(Integer.SIZE, BigInteger.valueOf(value).toByteArray());
    }

    public static BitVector fromBoolean(boolean value) {
        return new BitVector(1, value ? new byte[]{1} : new byte[]{0});
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

    public BitVector concat(BitVector other) {
        var result = new BitVector(size + other.size);
        int lhsLength = data.length, rhsLength = other.data.length;
        if ((other.size & 7) == 0) {
            System.arraycopy(other.data, 0, result.data, 0, rhsLength);
            System.arraycopy(data, 0, result.data, rhsLength, lhsLength);
        } else {
            normalize();
            int offset = other.size & 7;
            var shift = this.shl(offset);
            System.arraycopy(other.data, 0, result.data, 0, rhsLength - 1);
            System.arraycopy(shift.data, 1, result.data, rhsLength, lhsLength - 1);
            result.data[rhsLength - 1] = (byte) (shift.data[0] | other.data[rhsLength - 1]);
            result.data[result.data.length - 1] = (byte) (data[lhsLength - 1] << offset);
        }
        return result;
    }

    public BitVector negative() {
        BitVector result = not();
        for (int i = 0; i < data.length; i++) {
            result.data[i]++;
            if (result.data[i] != 0) {
                break;
            }
        }
        return result;
    }

    public BitVector not() {
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) ~data[i];
        }
        return result;
    }

    public BitVector add(BitVector other) {
        requiresSameSize(this, other);
        BitVector result = new BitVector(size);
        int carry = 0;
        for (int i = 0; i < data.length; i++) {
            int sum = (data[i] & 0xff) + (other.data[i] & 0xff) + carry;
            result.data[i] = (byte) sum;
            carry = sum >> 8;
        }
        return result;
    }

    public BitVector sub(BitVector other) {
        requiresSameSize(this, other);
        return add(other.negative());
    }

    public BitVector mul(BitVector other) {
        requiresSameSize(this, other);
        return fromBigInteger(size, toBigInteger().multiply(other.toBigInteger()));
    }

    public BitVector div(BitVector other) {
        requiresSameSize(this, other);
        return fromBigInteger(size, toBigInteger().divide(other.toBigInteger()));
    }

    public BitVector mod(BitVector other) {
        requiresSameSize(this, other);
        return fromBigInteger(size, toBigInteger().mod(other.toBigInteger()));
    }

    public BitVector pow(BitVector other) {
        return fromBigInteger(size, toBigInteger().pow(other.toInt()));
    }

    public BitVector and(BitVector other) {
        requiresSameSize(this, other);
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) (data[i] & other.data[i]);
        }
        return result;
    }

    public BitVector nand(BitVector other) {
        return this.and(other).not();
    }

    public BitVector or(BitVector other) {
        requiresSameSize(this, other);
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) (data[i] | other.data[i]);
        }
        return result;
    }

    public BitVector nor(BitVector other) {
        return this.or(other).not();
    }

    public BitVector xor(BitVector other) {
        requiresSameSize(this, other);
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) (data[i] ^ other.data[i]);
        }
        return result;
    }

    public BitVector xnor(BitVector other) {
        requiresSameSize(this, other);
        return xor(other).not();
    }

    public BitVector ashr(int offset) {
        var result = new BitVector(size);
        if (get(size - 1)) {
            int maskOffset = 8 - ((data.length << 3) - size);
            data[data.length - 1] |= (byte) (0xff << maskOffset);
            result.setAllBytes((byte) 0xff);
        }
        doShiftRight(result, offset);
        return result;
    }

    public BitVector lshr(int offset) {
        var result = new BitVector(size);
        doShiftRight(result, offset);
        return result;
    }

    private void doShiftRight(BitVector result, int offset) {
        int byteOffset = offset >> 3, bitOffset = offset & 7;
        if (byteOffset >= data.length) {
            return;
        }
        if (bitOffset == 0) {
            System.arraycopy(data, byteOffset, result.data, 0, data.length - byteOffset);
        } else {
            result.data[data.length - byteOffset - 1] = (byte) (data[data.length - 1] >> bitOffset);
            var mask = ((1 << (8 - bitOffset + 1)) - 1);
            for (int i = 0; i < data.length - byteOffset - 1; i++) {
                var low = (data[i + byteOffset] >>> bitOffset) & mask;
                var high = (data[i + byteOffset + 1] << (8 - bitOffset));
                result.data[i] = (byte) (low | high);
            }
        }
    }

    public BitVector shl(int offset) {
        BitVector result = new BitVector(size);
        int byteOffset = offset >> 3, bitOffset = offset & 7;
        if (byteOffset >= data.length) {
            return result;
        }
        if (bitOffset == 0) {
            System.arraycopy(data, 0, result.data, byteOffset, data.length);
        } else {
            result.data[byteOffset] = (byte) (data[0] << bitOffset);
            var mask = ((1 << (bitOffset + 1)) - 1);
            for (int i = byteOffset + 1; i < data.length; i++) {
                var low = data[i - byteOffset] << bitOffset;
                var high = (data[i - byteOffset - 1] >>> (8 - bitOffset)) & mask;
                result.data[i] = (byte) (low | high);
            }
        }
        return result;
    }

    public boolean eq(BitVector other) {
        if (size != other.size) {
            return false;
        }
        this.normalize();
        other.normalize();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != other.data[i]) {
                return false;
            }
        }
        return true;
    }

    public boolean ne(BitVector other) {
        return !eq(other);
    }

    public boolean sgt(BitVector other) {
        return toBigInteger().compareTo(other.toBigInteger()) > 0;
    }

    public boolean sge(BitVector other) {
        return toBigInteger().compareTo(other.toBigInteger()) >= 0;
    }

    public boolean slt(BitVector other) {
        return toBigInteger().compareTo(other.toBigInteger()) < 0;
    }

    public boolean sle(BitVector other) {
        return toBigInteger().compareTo(other.toBigInteger()) <= 0;
    }

    public boolean ugt(BitVector other) {
        return toUnsignedBigInteger().compareTo(other.toBigInteger()) > 0;
    }

    public boolean uge(BitVector other) {
        return toUnsignedBigInteger().compareTo(other.toBigInteger()) >= 0;
    }

    public boolean ult(BitVector other) {
        return toUnsignedBigInteger().compareTo(other.toBigInteger()) < 0;
    }

    public boolean ule(BitVector other) {
        return toUnsignedBigInteger().compareTo(other.toBigInteger()) <= 0;
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
        data[data.length - 1] &= mask;
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
