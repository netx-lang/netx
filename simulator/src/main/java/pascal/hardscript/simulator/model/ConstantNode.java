package pascal.hardscript.simulator.model;

import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;
import pascal.hardscript.util.lang.Identifier;

public final class ConstantNode extends DataNode {
    private ConstantNode(int width) {
        super(null, width);
    }

    public static ConstantNode of(BitVector value) {
        var result = new ConstantNode(value.size());
        result.value = value;
        return result;
    }

    @Override
    public Identifier getIdentifier() {
        throw new ShallNotReachHereError();
    }

    @Override
    public void setValue(BitVector value) {
        throw new ShallNotReachHereError();
    }
}
