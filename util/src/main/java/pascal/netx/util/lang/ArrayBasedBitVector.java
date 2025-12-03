package pascal.netx.util.lang;

import pascal.netx.util.exception.ShallNotReachHereError;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Objects;

/**
 * A BitVector implementation that uses a byte array as the underlying data structure.
 *
 * @param size The size of the bit vector in bits.
 * @param data The byte array representing the bits.
 *             Each byte in the array represents 8 bits, with the least significant bit of the first byte
 *             corresponding to index 0 of the bit vector. For example, the number 0xFFAA is represented as
 *             data[0] == 10101010 and data[1] == 11111111
 */
public record ArrayBasedBitVector(int size, byte[] data) implements BitVector {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    public ArrayBasedBitVector(int size, byte[] data) {
        this.size = size;
        this.data = normalize(size, data);
    }

    ArrayBasedBitVector(int size, BigInteger value) {
        var _data = new byte[arrayIndex(size - 1) + 1];

        var bytes = value.toByteArray();
        int length = Math.min(_data.length, bytes.length);
        for (int i = 0; i < length; i++) {
            _data[i] = bytes[bytes.length - 1 - i];
        }
        if (value.signum() < 0) {
            for (int i = length; i < _data.length; i++) {
                _data[i] = (byte) 0xFF;
            }
            _data[length - 1] |= (byte) (0xFF << byteOffset(value.bitLength()));
        }
        this(size, normalize(size, _data));
    }

    private static int into(byte _byte) {
        return _byte & 0xFF;
    }

    private static byte[] normalize(int size, byte[] data) {
        int last = arrayIndex(size - 1);
        int offset = byteOffset(size - 1);
        var result = new byte[last + 1];
        System.arraycopy(data, 0, result, 0, Math.min(data.length, result.length));
        if (offset != 7) {
            result[last] &= (byte) ((1 << (offset + 1)) - 1);
        }
        return result;
    }

    private static int arrayIndex(int bitIndex) {
        return bitIndex >> 3;
    }

    private static int byteOffset(int bitIndex) {
        return bitIndex & 0b111;
    }

    @Override
    public BigInteger asSignedBI() {
        if (msb()) {
            return neg().asUnsignedBI().negate();
        } else {
            return asUnsignedBI();
        }
    }

    @Override
    public BigInteger asUnsignedBI() {
        BigInteger result = BigInteger.ZERO;
        for (int i = data.length - 1; i >= 0; i--) {
            result = result.shiftLeft(8).or(BigInteger.valueOf(into(data[i])));
        }
        return result;
    }

    @Override
    @Nonnull
    public String toString() {
        char[] hexChars = new char[data.length * 2];
        for (int j = 0; j < data.length; j++) {
            int v = data[data.length - j - 1] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        String hex = new String(hexChars);
        int idx = 0;
        while (idx < hex.length() - 1 && hex.charAt(idx) == '0') {
            idx++;
        }
        hex = hex.substring(idx);
        return size + "'h" + hex;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        ArrayBasedBitVector that = (ArrayBasedBitVector) o;
        return eq(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(toString());
    }

    @Override
    public boolean get(int index) {
        assert 0 <= index && index < size;
        return (data[arrayIndex(index)] & (1 << byteOffset(index))) != 0;
    }

    @Override
    public boolean msb() {
        return get(size - 1);
    }

    @Override
    public boolean lsb() {
        return get(0);
    }

    @Override
    public boolean isZero() {
        for (byte b : data) {
            if (b != 0) {
                return false;
            }
        }
        return true;
    }

    @Override
    public ArrayBasedBitVector uext(int width) {
        assert width >= size;
        return new ArrayBasedBitVector(width, this.data);
    }

    @Override
    public ArrayBasedBitVector sext(int width) {
        assert width >= size;
        if (msb()) {
            int oldLast = arrayIndex(size - 1);
            int newLast = arrayIndex(width - 1);
            byte[] _data = new byte[newLast + 1];
            System.arraycopy(data, 0, _data, 0, data.length);
            for (int i = oldLast + 1; i <= newLast; i++) {
                _data[i] = (byte) 0xFF;
            }
            _data[oldLast] |= (byte) (0xFF << (byteOffset(size - 1) + 1));
            return new ArrayBasedBitVector(width, normalize(width, _data));
        } else {
            return uext(width);
        }
    }

    @Override
    public ArrayBasedBitVector truncate(int width) {
        assert width <= size;
        return new ArrayBasedBitVector(width, normalize(width, data));
    }

    @Override
    public BitVector select(int msb, int lsb) {
        assert 0 <= lsb && lsb <= msb && msb < size;
        int length = msb - lsb + 1;
        if (length >= SIZE_THRESHOLD) {
            return new ArrayBasedBitVector(length, normalize(length, lshr(lsb).data));
        } else {
            return new LongBasedBitVector(length, lshr(lsb).toULong());
        }
    }

    @Override
    public ArrayBasedBitVector copyOf() {
        return new ArrayBasedBitVector(size, data.clone());
    }

    @Override
    public int toUInt() {
        return Math.toIntExact(toULong());
    }

    @Override
    public long toULong() {
        long result = 0;
        for (int i = data.length - 1; i >= 0; i--) {
            result <<= 8;
            result |= into(data[i]);
        }
        return result;
    }

    @Override
    public ArrayBasedBitVector concat(BitVector that) {
        int width = this.size + that.size();
        byte[] _data = new byte[arrayIndex(width - 1) + 1];
        System.arraycopy(this.data, 0, _data, 0, this.data.length);
        return new ArrayBasedBitVector(width, _data).shl(that.size()).or(that.uext(width));
    }

    @Override
    public ArrayBasedBitVector neg() {
        byte[] _data = new byte[data.length];
        boolean carry = true;
        for (int i = 0; i < data.length; i++) {
            byte inverted = (byte) ~data[i];
            if (carry) {
                if (inverted == (byte) 0xFF) {
                    _data[i] = 0;
                } else {
                    _data[i] = (byte) (inverted + 1);
                    carry = false;
                }
            } else {
                _data[i] = inverted;
            }
        }
        return new ArrayBasedBitVector(size, normalize(size, _data));
    }

    @Override
    public ArrayBasedBitVector not() {
        byte[] _data = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            _data[i] = (byte) ~data[i];
        }
        return new ArrayBasedBitVector(size, normalize(size, _data));
    }

    @Override
    public ArrayBasedBitVector add(BitVector that) {
        if (that instanceof ArrayBasedBitVector(int _size, var _data)) {
            assert size == _size;
            byte[] result = new byte[data.length];
            int carry = 0;
            for (int i = 0; i < data.length; i++) {
                int sum = into(data[i]) + into(_data[i]) + carry;
                result[i] = (byte) (sum & 0xFF);
                carry = sum >>> 8;
            }
            return new ArrayBasedBitVector(size, normalize(size, result));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector sub(BitVector that) {
        return add(that.neg());
    }

    @Override
    public ArrayBasedBitVector mul(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            assert size == abv.size;
            return new ArrayBasedBitVector(size, asUnsignedBI().multiply(abv.asUnsignedBI()));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector div(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            assert size == abv.size;
            return new ArrayBasedBitVector(size, asUnsignedBI().divide(abv.asUnsignedBI()));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector mod(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            assert size == abv.size;
            return new ArrayBasedBitVector(size, asUnsignedBI().mod(abv.asUnsignedBI()));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector pow(BitVector that) {
        return new ArrayBasedBitVector(size, asUnsignedBI().pow(that.toUInt()));
    }

    @Override
    public ArrayBasedBitVector and(BitVector that) {
        if (that instanceof ArrayBasedBitVector(int _size, var _data)) {
            assert size == _size;
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] & _data[i]);
            }
            return new ArrayBasedBitVector(size, normalize(size, result));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector or(BitVector that) {
        if (that instanceof ArrayBasedBitVector(int _size, var _data)) {
            assert size == _size;
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] | _data[i]);
            }
            return new ArrayBasedBitVector(size, normalize(size, result));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector xor(BitVector that) {
        if (that instanceof ArrayBasedBitVector(int _size, var _data)) {
            assert size == _size;
            byte[] result = new byte[data.length];
            for (int i = 0; i < data.length; i++) {
                result[i] = (byte) (data[i] ^ _data[i]);
            }
            return new ArrayBasedBitVector(size, normalize(size, result));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public ArrayBasedBitVector xnor(BitVector that) {
        return this.xor(that).not();
    }

    @Override
    public ArrayBasedBitVector ashr(int offset) {
        if (msb()) {
            var rep = asUnsignedBI();
            var high = BigInteger.ONE.shiftLeft(size + 1).subtract(BigInteger.ONE);
            var low = BigInteger.ONE.shiftLeft(size - offset).subtract(BigInteger.ONE);
            return new ArrayBasedBitVector(size, rep.shiftRight(offset).or(high.xor(low)));
        } else {
            return lshr(offset);
        }
    }

    @Override
    public ArrayBasedBitVector lshr(int offset) {
        return new ArrayBasedBitVector(size, asUnsignedBI().shiftRight(offset));
    }

    @Override
    public ArrayBasedBitVector shl(int offset) {
        return new ArrayBasedBitVector(size, asUnsignedBI().shiftLeft(offset));
    }

    @Override
    public boolean eq(BitVector that) {
        if (size == that.size()) {
            return switch (that) {
                case LongBasedBitVector lbv -> toULong() == lbv.toULong();
                case ArrayBasedBitVector abv -> asUnsignedBI().equals(abv.asUnsignedBI());
            };
        }
        return false;
    }

    @Override
    public boolean ne(BitVector that) {
        return !eq(that);
    }

    @Override
    public boolean sgt(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asSignedBI().compareTo(abv.asSignedBI()) > 0;
        }
        return false;
    }

    @Override
    public boolean sge(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asSignedBI().compareTo(abv.asSignedBI()) >= 0;
        }
        return false;
    }

    @Override
    public boolean slt(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asSignedBI().compareTo(abv.asSignedBI()) < 0;
        }
        return false;
    }

    @Override
    public boolean sle(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asSignedBI().compareTo(abv.asSignedBI()) <= 0;
        }
        return false;
    }

    @Override
    public boolean ugt(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asUnsignedBI().compareTo(abv.asUnsignedBI()) > 0;
        }
        return false;
    }

    @Override
    public boolean uge(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asUnsignedBI().compareTo(abv.asUnsignedBI()) >= 0;
        }
        return false;
    }

    @Override
    public boolean ult(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asUnsignedBI().compareTo(abv.asUnsignedBI()) < 0;
        }
        return false;
    }

    @Override
    public boolean ule(BitVector that) {
        if (that instanceof ArrayBasedBitVector abv) {
            return asUnsignedBI().compareTo(abv.asUnsignedBI()) <= 0;
        }
        return false;
    }
}
