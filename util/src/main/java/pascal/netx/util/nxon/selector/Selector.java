package pascal.netx.util.nxon.selector;

import pascal.netx.util.lang.BitVector;

public interface Selector {
    int getWidth();

    BitVector apply(BitVector vector);

    BitVector inverseApply(BitVector base, BitVector vector);
}
