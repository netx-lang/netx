package pascal.netx.compiler.model.variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.variable.selector.BitSelector;
import pascal.netx.compiler.model.variable.selector.PartSelector;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.PolyType;
import pascal.netx.compiler.model.variable.type.Type;

import java.util.LinkedList;
import java.util.List;

public sealed interface Variable extends Entity permits Mutable, Value {

    Logger log = LoggerFactory.getLogger(Variable.class);

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
                        log.error("Array {} cannot be accessed with a part selector {}", array, selector);
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
