package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.variable.selector.BitSelector;
import pascal.netx.compiler.circuit.variable.selector.PartSelector;
import pascal.netx.compiler.circuit.variable.selector.Selector;
import pascal.netx.compiler.circuit.variable.type.PolyType;
import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;

import java.util.LinkedList;
import java.util.List;

public sealed interface Variable extends Entity permits Mutable, Value {

    static Variable accessOf(Variable base, Selector selector) {
        switch (base) {
            case Value value -> {
                return selectConstant(value, selector);
            }
            case Array array -> {
                switch (selector) {
                    case BitSelector(var index) -> {
                        return array.getElements().get(index.toInt());
                    }
                    case PartSelector _ -> {
                        Logger.error("Array {} cannot be accessed with a part selector {}", array, selector);
                        throw new CompileCancellationException();
                    }
                }
            }
            case Access _ -> {
                List<Selector> selectors = new LinkedList<>();
                selectors.add(selector);
                while (base instanceof Access(Data nextBase, Selector nextSelector)) {
                    base = nextBase;
                    selectors.addFirst(nextSelector);
                }
                var finalSelector = selectors.getFirst();
                for (int i = 1; i < selectors.size(); i++) {
                    finalSelector = finalSelector.refine(selectors.get(i));
                }
                return new Access((Data) base, finalSelector);
            }
            default -> {
                return new Access((Data) base, selector);
            }
        }
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

    Type getType();

    void setType(Type type);

    default boolean hasImplicitType() {
        return getType() instanceof PolyType;
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }

    boolean isBuiltin();

}
