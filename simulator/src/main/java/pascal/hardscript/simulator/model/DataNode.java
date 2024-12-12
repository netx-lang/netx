package pascal.hardscript.simulator.model;

import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;

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
