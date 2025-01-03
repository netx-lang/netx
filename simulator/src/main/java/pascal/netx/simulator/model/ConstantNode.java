package pascal.netx.simulator.model;

import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.Identifier;

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
