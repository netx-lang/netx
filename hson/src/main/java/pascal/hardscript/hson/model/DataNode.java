package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;

public abstract class DataNode extends Node {
    private BitVector value;
    private final int width;

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
