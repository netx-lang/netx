package pascal.netx.simulator.model;

import pascal.netx.util.nxon.selector.Selector;
import pascal.netx.util.lang.BitVector;

public final class DataSelectNode extends DataNode {
    private final DataNode base;
    private final Selector selector;

    private DataSelectNode(DataNode base, Selector selector) {
        super(base.getIdentifier(), selector.getWidth());
        this.base = base;
        this.selector = selector;
    }

    public static DataSelectNode of(DataNode node, Selector selector) {
        return new DataSelectNode(node, selector);
    }

    public DataNode getBase() {
        return base;
    }

    public BitVector apply() {
        return selector.apply(base.getValue());
    }

    public BitVector inverseApply() {
        return selector.inverseApply(base.getValue(), getValue());
    }
}
