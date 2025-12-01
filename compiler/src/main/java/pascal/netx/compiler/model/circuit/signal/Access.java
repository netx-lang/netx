package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.io.Logger;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.signal.selector.Selector;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.compiler.utils.TypeCheckingException;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;
import pascal.netx.util.lang.SourceLoc;

import javax.annotation.Nonnull;
import java.util.Objects;

public record Access(Wire base, Selector selector) implements Data {

    @Override
    public Type getType() {
        return BitType.of(selector.getWidth());
    }

    @Override
    public void setType(Type type) {
        Logger.error("DataAccess {} has an immutable type", this);
        throw new TypeCheckingException();
    }

    @Override
    public boolean isBuiltin() {
        return base.isBuiltin();
    }

    @Override
    public SourceLoc getSourceLoc() {
        return base.getSourceLoc();
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Access access = (Access) o;
        return Objects.equals(base, access.base) && Objects.equals(selector, access.selector);
    }

    @Override
    public int hashCode() {
        return Objects.hash(base, selector);
    }

    @Override
    public @Nonnull String toString() {
        return base.toString() + selector;
    }
}
