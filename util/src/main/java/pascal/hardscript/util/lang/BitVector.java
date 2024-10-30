package pascal.hardscript.util.lang;

import pascal.hardscript.util.exception.TodoException;

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
        Arrays.fill(data, (byte) 0);
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

    public int toInt() {
        return toBigInteger().intValue();
    }

    public BitVector negative() {
        BitVector result = invert();
        for (int i = 0; i < data.length; i++) {
            result.data[i]++;
            if (result.data[i] != 0) {
                break;
            }
        }
        return result;
    }

    public BitVector invert() {
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) ~data[i];
        }
        return result;
    }

    public BitVector add(BitVector other) {
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
        return add(other.negative());
    }

    public BitVector pow(BitVector other) {
        return fromBigInteger(size, toBigInteger().pow(other.toInt()));
    }

    public BitVector and(BitVector other) {
        BitVector result = new BitVector(size);
        for (int i = 0; i < data.length; i++) {
            result.data[i] = (byte) (data[i] & other.data[i]);
        }
        return result;
    }

    public BitVector ahsr(int offset) {
        throw new TodoException();
    }

    public BitVector lshr(int offset) {
        throw new TodoException();
    }

    public BitVector shl(int offset) {
        BitVector result = new BitVector(size);
        int byteOffset = offset >> 3, bitOffset = offset & 7;
        for (int i = 0; i < data.length - byteOffset; i++) {
            result.data[i + byteOffset] |= (byte) (data[i] << bitOffset);
            if (bitOffset > 0 && i + byteOffset + 1 < result.data.length) {
                result.data[i + byteOffset + 1] |= (byte) (data[i] >> (8 - bitOffset));
            }
        }
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;

        var that = (BitVector) obj;
        this.normalize();
        that.normalize();
        for (int i = 0; i < data.length; i++) {
            if (data[i] != that.data[i]) {
                return false;
            }
        }
        return true;
    }

    private void normalize() {
        int offset = 8 - ((data.length << 3) - size);
        var mask = (byte) ((1 << offset) - 1);
        data[data.length - 1] &= mask;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (byte datum : data) {
            for (int i = 0; i < 8; i++) {
                sb.append((datum & (1 << i)) != 0 ? '1' : '0');
            }
        }
        return sb.reverse().substring(data.length * 8 - size);
    }
}
