package pascal.netx.util.lang;

import pascal.netx.util.exception.ShallNotReachHereError;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.util.Objects;

public record LongBasedBitVector(int size, long rep) implements BitVector {
    public LongBasedBitVector(int size, long rep) {
        this.size = size;
        this.rep = normalize(size, rep);
    }

    private static long normalize(int size, long rep) {
        return rep & ((1L << size) - 1);
    }

    private long asJavaLong() {
        return msb() ? (rep | (~0L) << size) : rep;
    }

    @Override
    @Nonnull
    public String toString() {
        return size + "'h" + Long.toHexString(rep).toUpperCase();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        LongBasedBitVector that = (LongBasedBitVector) o;
        return eq(that);
    }

    @Override
    public int hashCode() {
        return Objects.hash(size, rep);
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public boolean isZero() {
        return rep == 0;
    }

    @Override
    public boolean get(int index) {
        assert 0 <= index && index < size;
        return (rep & (1L << index)) != 0;
    }

    @Override
    public boolean msb() {
        return (rep & (1L << (size - 1))) != 0;
    }

    @Override
    public boolean lsb() {
        return (rep & 1L) != 0;
    }

    @Override
    public BitVector uext(int width) {
        assert width >= size;
        if (width >= SIZE_THRESHOLD) {
            return new ArrayBasedBitVector(size, BigInteger.valueOf(rep)).uext(width);
        } else {
            return new LongBasedBitVector(width, rep);
        }
    }

    @Override
    public BitVector sext(int width) {
        assert width >= size;
        if (width >= SIZE_THRESHOLD) {
            return new ArrayBasedBitVector(size, BigInteger.valueOf(rep)).sext(width);
        } else {
            return new LongBasedBitVector(width, asJavaLong());
        }
    }

    @Override
    public LongBasedBitVector truncate(int width) {
        assert width <= size;
        return new LongBasedBitVector(width, normalize(width, rep));
    }

    @Override
    public LongBasedBitVector select(int msb, int lsb) {
        assert 0 <= lsb && lsb <= msb && msb < size;
        return this.lshr(lsb).truncate(msb - lsb + 1);
    }

    @Override
    public BitVector copyOf() {
        return new LongBasedBitVector(size, rep);
    }

    @Override
    public int toUInt() {
        return Math.toIntExact(rep);
    }

    @Override
    public long toULong() {
        return rep;
    }

    @Override
    public BigInteger asSignedBI() {
        return BigInteger.valueOf(asJavaLong());
    }

    @Override
    public BigInteger asUnsignedBI() {
        return BigInteger.valueOf(toULong());
    }

    @Override
    public BitVector concat(BitVector that) {
        int width = this.size + that.size();
        if (width >= SIZE_THRESHOLD) {
            return new ArrayBasedBitVector(this.size, BigInteger.valueOf(toULong())).concat(that);
        } else {
            return new LongBasedBitVector(width, rep << that.size() | ((LongBasedBitVector) that).rep);
        }
    }

    @Override
    public LongBasedBitVector neg() {
        return new LongBasedBitVector(size, normalize(size, -rep));
    }

    @Override
    public LongBasedBitVector not() {
        return new LongBasedBitVector(size, ~rep);
    }

    @Override
    public LongBasedBitVector add(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep + _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector sub(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep - _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector mul(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep * _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector div(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep / _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector mod(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep % _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector pow(BitVector that) {
        return new LongBasedBitVector(size, Math.powExact(rep, that.toUInt()));
    }

    @Override
    public LongBasedBitVector and(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep & _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector nand(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, ~(this.rep & _rep));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector or(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep | _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector nor(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, ~(this.rep | _rep));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector xor(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, this.rep ^ _rep);
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector xnor(BitVector that) {
        if (that instanceof LongBasedBitVector(var _size, var _rep)) {
            assert this.size == _size;
            return new LongBasedBitVector(size, ~(this.rep ^ _rep));
        }
        throw new ShallNotReachHereError();
    }

    @Override
    public LongBasedBitVector ashr(int offset) {
        return new LongBasedBitVector(size, asJavaLong() >> offset);
    }

    @Override
    public LongBasedBitVector lshr(int offset) {
        return new LongBasedBitVector(size, rep >>> offset);
    }

    @Override
    public LongBasedBitVector shl(int offset) {
        return new LongBasedBitVector(size, rep << offset);
    }

    @Override
    public boolean eq(BitVector that) {
        if (size == that.size()) {
            return switch (that) {
                case LongBasedBitVector lbv -> this.rep == lbv.rep;
                case ArrayBasedBitVector abv -> abv.eq(this);
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
        if (that instanceof LongBasedBitVector lbv) {
            assert this.size == lbv.size;
            return asJavaLong() > lbv.asJavaLong();
        }
        return that.sgt(this);
    }

    @Override
    public boolean sge(BitVector that) {
        if (that instanceof LongBasedBitVector lbv) {
            assert this.size == lbv.size;
            return asJavaLong() >= lbv.asJavaLong();
        }
        return that.sge(this);
    }

    @Override
    public boolean slt(BitVector that) {
        if (that instanceof LongBasedBitVector lbv) {
            assert this.size == lbv.size;
            return asJavaLong() < lbv.asJavaLong();
        }
        return that.slt(this);
    }

    @Override
    public boolean sle(BitVector that) {
        if (that instanceof LongBasedBitVector lbv) {
            assert this.size == lbv.size;
            return asJavaLong() <= lbv.asJavaLong();
        }
        return that.sle(this);
    }

    @Override
    public boolean ugt(BitVector that) {
        if (that instanceof LongBasedBitVector(int _size, var _rep)) {
            assert this.size == _size;
            return Long.compareUnsigned(this.rep, _rep) > 0;
        }
        return that.ugt(this);
    }

    @Override
    public boolean uge(BitVector that) {
        if (that instanceof LongBasedBitVector(int _size, var _rep)) {
            assert this.size == _size;
            return Long.compareUnsigned(this.rep, _rep) >= 0;
        }
        return that.uge(this);
    }

    @Override
    public boolean ult(BitVector that) {
        if (that instanceof LongBasedBitVector(int _size, var _rep)) {
            assert this.size == _size;
            return Long.compareUnsigned(this.rep, _rep) < 0;
        }
        return that.ult(this);
    }

    @Override
    public boolean ule(BitVector that) {
        if (that instanceof LongBasedBitVector(int _size, var _rep)) {
            assert this.size == _size;
            return Long.compareUnsigned(this.rep, _rep) <= 0;
        }
        return that.ule(this);
    }
}
