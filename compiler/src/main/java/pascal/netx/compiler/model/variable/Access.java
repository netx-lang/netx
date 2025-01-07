package pascal.netx.compiler.model.variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.TypeCheckingException;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.selector.Selector;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

public record Access(Data base, Selector selector) implements Mutable {
    private static final Logger log = LoggerFactory.getLogger(Access.class);

    @Override
    public Type getType() {
        return BitType.of(selector.getWidth());
    }

    @Override
    public void setType(Type type) {
        log.error("{}: DataAccess {} has an immutable type", base.getSourceLoc(), this);
        throw new TypeCheckingException();
    }

    @Override
    public boolean isBuiltin() {
        return base.isBuiltin();
    }

    @Override
    public int getIndex() {
        return base.getIndex();
    }

    @Override
    public Identifier getIdentifier() {
        throw new ShallNotReachHereError();
    }

    @Override
    public ComponentScope getParent() {
        return base.getParent();
    }
}
