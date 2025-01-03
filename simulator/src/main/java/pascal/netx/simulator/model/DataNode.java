package pascal.netx.simulator.model;

import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

public abstract sealed class DataNode extends Node permits ClockNode, ConstantNode, WireNode, DataSelectNode {
    protected final int width;
    protected BitVector value;

    protected DataNode(Identifier id, int width) {
        super(id);
        this.width = width;
    }

    public BitVector getValue() {
        return value;
    }

    public void setValue(BitVector value) {
        this.value = value;
    }

    public int getWidth() {
        return width;
    }
}
