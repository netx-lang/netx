package pascal.netx.util.hson.selector;

import pascal.netx.util.lang.BitVector;

public interface Selector {
    int getWidth();

    BitVector apply(BitVector vector);

    BitVector inverseApply(BitVector base, BitVector vector);
}
