package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.BitVector;

public abstract class DataNode implements Node {
    private BitVector value;

    public void setValue(BitVector value) {
        this.value = value;
    }

    public BitVector getValue() {
        return value;
    }
}
