package pascal.hardscript.hson.util.selector;

import pascal.hardscript.util.lang.BitVector;

public record BitSelector(int index) implements Selector {
    public static BitSelector of(int index) {
        return new BitSelector(index);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public BitVector apply(BitVector vector) {
        return BitVector.fromBoolean(vector.get(index));
    }

    @Override
    public BitVector inverseApply(BitVector base, BitVector vector) {
        var result = base.copyOf();
        if (vector.toBoolean()) {
            result.set(index);
        } else {
            result.clear(index);
        }
        return result;
    }

    @Override
    public String toString() {
        return "[" + index + ']';
    }
}
