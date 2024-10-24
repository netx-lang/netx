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
        System.arraycopy(data, 0, this.data, 0, Math.min(data.length, this.data.length));
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

    public void copy(BitVector other) {
        System.arraycopy(other.data, 0, data, 0, data.length);
    }

    public void or(BitVector other) {
        for (int i = 0; i < data.length; i++) {
            data[i] |= other.data[i];
        }
    }

    public void and(BitVector other) {
        for (int i = 0; i < data.length; i++) {
            data[i] &= other.data[i];
        }
    }

    public void xor(BitVector other) {
        for (int i = 0; i < data.length; i++) {
            data[i] ^= other.data[i];
        }
    }

    public boolean equals(BitVector other) {
        for (int i = 0; i < data.length; i++) {
            if (data[i] != other.data[i]) {
                return false;
            }
        }
        return true;
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
