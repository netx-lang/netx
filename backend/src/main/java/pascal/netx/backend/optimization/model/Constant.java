package pascal.netx.backend.optimization.model;

import pascal.netx.util.lang.BitVector;

public final class Constant extends AbstractVertex{
    private final BitVector value;

    public Constant(int index, BitVector value) {
        super(index);
        this.value = value;
    }

    public BitVector value() {
        return value;
    }

    public boolean isZero() {
        return value.isZero();
    }

    public boolean isMersenne() {
        return value.not().isZero();
    }

    @Override
    public String name() {
        return value.toString();
    }
}
