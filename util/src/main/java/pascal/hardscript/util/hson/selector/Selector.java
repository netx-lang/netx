package pascal.hardscript.util.hson.selector;

import pascal.hardscript.util.lang.BitVector;

public interface Selector {
    int getWidth();

    BitVector apply(BitVector vector);

    BitVector inverseApply(BitVector base, BitVector vector);
}
