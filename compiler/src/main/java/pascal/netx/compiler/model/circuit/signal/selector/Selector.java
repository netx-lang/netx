package pascal.netx.compiler.model.circuit.signal.selector;

import pascal.netx.compiler.model.circuit.EList;
import pascal.netx.compiler.model.circuit.Entity;

public sealed interface Selector permits PartSelector, BitSelector {
    int getWidth();

    static Selector ofBit(int index) {
        return new BitSelector(index);
    }

    static Selector ofWidth(int width) {
        if (width == 1) {
            return new BitSelector(0);
        }
        return new PartSelector(width - 1, 0);
    }

    Selector refine(Selector selector);

    int msb();

    int lsb();

    Entity applyOn(EList list);
}
