package pascal.hardscript.compiler.model.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.selector.BitSelector;
import pascal.hardscript.compiler.model.instance.selector.PartSelector;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public final class DataAccess extends Data {
    private static final Logger log = LoggerFactory.getLogger(DataAccess.class);
    private final Instance base;
    private final Selector selector;

    public DataAccess(Data base, Selector selector) {
        super(base.getIdentifier(), base.getParent(), base.getIndex());
        this.base = base;
        this.selector = selector;
    }

    public static Instance of(Instance base, Selector selector) {
        if (base instanceof Value value) {
            return selectConstant(value, selector);
        } else if (!(base instanceof DataAccess)) {
            return new DataAccess((Data) base, selector);
        }

        List<Selector> selectors = new LinkedList<>();
        selectors.add(selector);
        while (base instanceof DataAccess access) {
            base = access.getBase();
            selectors.addFirst(access.getSelector());
        }
        var finalSelector = selectors.getFirst();
        for (int i = 1; i < selectors.size(); i++) {
            finalSelector = finalSelector.refine(selectors.get(i));
        }
        if (base instanceof Value value) {
            return selectConstant(value, finalSelector);
        } else {
            return new DataAccess((Data) base, finalSelector);
        }
    }

    public Instance getBase() {
        return base;
    }

    public Selector getSelector() {
        return selector;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DataAccess that = (DataAccess) o;
        return Objects.equals(base, that.base) && Objects.equals(selector, that.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, selector);
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

    @Override
    public InstanceType getType() {
        return BitType.of(selector.getWidth());
    }

    @Override
    public void setType(InstanceType type) {
        log.error("{}: DataAccess {} has an immutable type", getIdentifier().getSourceLoc(), this);
        throw new TypeCheckingException();
    }
}
