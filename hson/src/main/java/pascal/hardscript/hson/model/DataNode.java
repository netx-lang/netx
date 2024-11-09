package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;

public abstract sealed class DataNode extends Node permits ClockNode, ConstantNode, WireNode {
    protected BitVector value;
    protected final int width;

    protected DataNode(Identifier id, int width) {
        super(id);
        this.width = width;
    }

    public void setValue(BitVector value) {
        this.value = value;
    }

    public BitVector getValue() {
        return value;
    }

    public int getWidth() {
        return width;
    }
}
