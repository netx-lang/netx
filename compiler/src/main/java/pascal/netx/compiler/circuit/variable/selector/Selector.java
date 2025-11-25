package pascal.netx.compiler.circuit.variable.selector;

import pascal.netx.compiler.circuit.variable.Int32Value;
import pascal.netx.compiler.circuit.variable.IntegerValue;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();

    IntegerValue msb();

    IntegerValue lsb();

    Selector refine(Selector selector);

    static Selector ofBit(int index) {
        return new BitSelector(Int32Value.of(index));
    }

    static Selector ofWidth(int width) {
        if (width == 1) {
            return new BitSelector(IntegerValue.ZERO);
        }
        return new PartSelector(Int32Value.of(width - 1), IntegerValue.ZERO);
    }
}
