package pascal.hardscript.util.lang;

import java.util.Arrays;

public final class BitVector {

    private final byte[] data;

    private final int size;

    public BitVector(int size) {
        this.size = size;
        this.data = new byte[(size + 7) / 8];
    }

    public static BitVector parse(String rep, int size, int radix) {
        BitVector result = new BitVector(size);
        for (int i = 0; i < rep.length(); i++) {
            int digit = Character.digit(rep.charAt(i), radix);
            for (int j = 0; j < 4; j++) {
                if ((digit & (1 << j)) != 0 && i * 4 + j < size) {
                    result.set(i * 4 + j);
                }
            }
        }
        return result;
    }

    public void set(int index) {
        data[index >> 3] |= (byte) (1 << (index & 8));
    }

    public void clear(int index) {
        data[index >> 3] &= (byte) ~(1 << (index & 8));
    }

    public boolean get(int index) {
        return (data[index >> 3] & (1 << (index & 8))) != 0;
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
            sb.append(datum);
        }
        return sb.reverse().substring(data.length * 8 - size);
    }
}
