package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.model.circuit.signal.selector.BitSelector;
import pascal.netx.compiler.model.circuit.signal.selector.PartSelector;
import pascal.netx.compiler.model.circuit.signal.selector.Selector;
import pascal.netx.compiler.model.type.Hole;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.util.lang.SourceLoc;

import java.util.LinkedList;
import java.util.List;

public sealed interface Signal extends Entity permits Data, Value {
    static Signal accessOf(Signal base, Selector selector) {
        switch (base) {
            case Value value -> {
                return selectConstant(value, selector);
            }
            case Array array -> {
                switch (selector) {
                    case BitSelector(var index) -> {
                        return array.getElements().get(index);
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
                while (base instanceof Access(Wire nextBase, Selector nextSelector)) {
                    base = nextBase;
                    selectors.addFirst(nextSelector);
                }
                var finalSelector = selectors.getFirst();
                for (int i = 1; i < selectors.size(); i++) {
                    finalSelector = finalSelector.refine(selectors.get(i));
                }
                return new Access((Wire) base, finalSelector);
            }
            default -> {
                return new Access((Wire) base, selector);
            }
        }
    }

    private static VectorValue selectConstant(Value value, Selector selector) {
        return VectorValue.of(value.toBitVector().select(selector.msb(), selector.lsb()));
    }

    Type getType();

    void setType(Type type);

    default boolean hasImplicitType() {
        return getType() instanceof Hole;
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }

    boolean isBuiltin();

    SourceLoc getSourceLoc();

}
