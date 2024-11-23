package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.instance.selector.PartSelector;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.PolyType;

import java.util.LinkedList;
import java.util.List;

public sealed interface Instance extends Entity permits MutableInstance, Value {

    static Instance accessOf(Instance base, Selector selector) {
        if (base instanceof Value value) {
            return selectConstant(value, selector);
        } else if (!(base instanceof DataAccess)) {
            return new DataAccess((Data) base, selector);
        }

        List<Selector> selectors = new LinkedList<>();
        selectors.add(selector);
        while (base instanceof DataAccess(Data nextBase, Selector nextSelector)) {
            base = nextBase;
            selectors.addFirst(nextSelector);
        }
        var finalSelector = selectors.getFirst();
        for (int i = 1; i < selectors.size(); i++) {
            finalSelector = finalSelector.refine(selectors.get(i));
        }
        return new DataAccess((Data) base, finalSelector);
    }

    private static BitVectorValue selectConstant(Value value, Selector selector) {
        int msb, lsb;
        switch (selector) {
            case BitSelector(var index) -> msb = lsb = index.toInt();
            case PartSelector(var f, var t) -> {
                // FIXME: big endian?
                msb = Math.max(f.toInt(), t.toInt());
                lsb = Math.min(f.toInt(), t.toInt());
            }
        }
        return BitVectorValue.of(value.toBitVector().select(msb, lsb));
    }

    InstanceType getType();

    void setType(InstanceType type);

    default boolean hasImplicitType() {
        return getType() instanceof PolyType;
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }

    boolean isBuiltin();
}
