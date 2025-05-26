package pascal.netx.compiler.model.variable.selector;

import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.IntegerValue;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();

    IntegerValue msb();

    IntegerValue lsb();

    Selector refine(Selector selector);

    static Selector ofWidth(int width) {
        if (width == 1) {
            return new BitSelector(IntegerValue.ZERO);
        }
        return new PartSelector(Int32Value.of(width - 1), IntegerValue.ZERO);
    }
}
