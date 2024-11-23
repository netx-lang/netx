package pascal.hardscript.compiler.model.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.instance.selector.Selector;
import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

public record DataAccess(Data base, Selector selector) implements MutableInstance {
    private static final Logger log = LoggerFactory.getLogger(DataAccess.class);

    @Override
    public InstanceType getType() {
        return BitType.of(selector.getWidth());
    }

    @Override
    public void setType(InstanceType type) {
        log.error("{}: DataAccess {} has an immutable type", base.getSourceLoc(), this);
        throw new TypeCheckingException();
    }

    @Override
    public boolean isBuiltin() {
        return base.isBuiltin();
    }
}
