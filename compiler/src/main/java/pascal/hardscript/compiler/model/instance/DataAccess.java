package pascal.hardscript.compiler.model.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;

public record DataAccess(Instance base, Selector selector) implements Data {

    private static final Logger log = LoggerFactory.getLogger(DataAccess.class);

    public static DataAccess of(Instance base, Selector selector) {
        return new DataAccess(base, selector);
    }

    @Override
    public Identifier getIdentifier() {
        return base.getIdentifier();
    }

    @Override
    public Scope getParent() {
        // FIXME: check
        return base.getParent();
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
