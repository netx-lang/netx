package pascal.netx.util.lang;

import pascal.netx.util.exception.ShallNotReachHereError;

import java.math.BigInteger;

public sealed interface BitVector permits LongBasedBitVector, ArrayBasedBitVector {

    Integer SIZE_THRESHOLD = 64;

    static BitVector parse(String literal) {
        var parts = literal.toLowerCase().split("'");
        int size = Integer.parseInt(parts[0]);
        int radix = switch (parts[1].charAt(0)) {
            case 'b' -> 2;
            case 'o' -> 8;
            case 'd' -> 10;
            case 'h' -> 16;
            default -> throw new ShallNotReachHereError();
        };
        return BitVector.parse(parts[1].substring(1), size, radix);
    }

    static BitVector parse(String literal, int size, int radix) {
        return fromBigInteger(size, new BigInteger(literal, radix));
    }

    static BitVector fromInt(int value) {
        return new LongBasedBitVector(32, value);
    }

    static BitVector fromInt(int size, int value){
        return new LongBasedBitVector(size, value);
    }

    static BitVector fromBoolean(boolean value){
        return new LongBasedBitVector(1, value ? 1L : 0L);
    }

    static BitVector fromBigInteger(int size, BigInteger value) {
        if (size <= 64) {
            return new LongBasedBitVector(size, value.longValue());
        } else {
            return new ArrayBasedBitVector(size, value);
        }
    }

    int size();

    boolean get(int index);

    boolean msb();

    boolean lsb();

    boolean isZero();

    BitVector uext(int width);

    BitVector sext(int width);

    BitVector truncate(int width);

    BitVector select(int msb, int lsb);

    BitVector copyOf();

    int toInt();

    long toLong();

    BigInteger toBigInteger();

    BitVector concat(BitVector that);

    BitVector neg();

    BitVector not();

    BitVector add(BitVector that);

    BitVector sub(BitVector that);

    BitVector mul(BitVector that);

    BitVector div(BitVector that);

    BitVector mod(BitVector that);

    BitVector pow(BitVector that);

    BitVector and(BitVector that);

    BitVector nand(BitVector that);

    BitVector or(BitVector that);

    BitVector nor(BitVector that);

    BitVector xor(BitVector that);

    BitVector xnor(BitVector that);

    BitVector ashr(int offset);

    BitVector lshr(int offset);

    BitVector shl(int offset);

    boolean eq(BitVector that);

    boolean ne(BitVector that);

    boolean sgt(BitVector that);

    boolean sge(BitVector that);

    boolean slt(BitVector that);

    boolean sle(BitVector that);

    boolean ugt(BitVector that);

    boolean uge(BitVector that);

    boolean ult(BitVector that);

    boolean ule(BitVector that);
}
