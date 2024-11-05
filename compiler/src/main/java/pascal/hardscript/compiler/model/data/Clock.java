package pascal.hardscript.compiler.model.data;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.exception.TypeCheckingException;
import pascal.hardscript.compiler.model.scope.Scope;

public record Clock(Identifier id, Scope parent) implements NonValue {

    private static final BitType TYPE = new BitType(1);
    private static final Logger log = LoggerFactory.getLogger(Clock.class);

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public DataType getType() {
        return TYPE;
    }

    @Override
    public void setType(DataType type) {
        log.error("{}: Clock {} has an immutable type", getIdentifier().getSourceLoc(), this);
        throw new TypeCheckingException();
    }

    public static class Factory implements NonValue.Factory<Clock> {
        private final Scope parent;

        public Factory(Scope parent) {
            this.parent = parent;
        }

        public static Factory of(Scope parent) {
            return new Factory(parent);
        }

        @Override
        public Clock create(Identifier id) {
            return new Clock(id, parent);
        }
    }
}
