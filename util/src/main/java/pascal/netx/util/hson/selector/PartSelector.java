package pascal.netx.util.hson.selector;

import pascal.netx.util.lang.BitVector;

public record PartSelector(int msb, int lsb) implements Selector {
    public static PartSelector of(int from, int to) {
        return new PartSelector(from, to);
    }

    @Override
    public int getWidth() {
        return Math.abs(msb - lsb) + 1;
    }

    @Override
    public BitVector apply(BitVector vector) {
        return vector.select(msb, lsb);
    }

    @Override
    public BitVector inverseApply(BitVector base, BitVector vector) {
        return base.set(msb, lsb, vector);
    }

    @Override
    public String toString() {
        return "[" + msb + ":" + lsb + ']';
    }
}
