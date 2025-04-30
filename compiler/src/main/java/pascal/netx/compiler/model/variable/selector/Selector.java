package pascal.netx.compiler.model.variable.selector;

import pascal.netx.compiler.model.variable.IntegerValue;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();

    IntegerValue msb();

    IntegerValue lsb();

    Selector refine(Selector selector);
}
