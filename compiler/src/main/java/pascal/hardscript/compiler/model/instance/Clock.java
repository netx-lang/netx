package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

public record Clock(Identifier id, Scope parent) implements Data {

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public Scope getParent() {
        return parent;
    }

    @Override
    public InstanceType getType() {
        return BitType.forClock();
    }

    @Override
    public void setType(InstanceType type) {
        throw new ShallNotReachHereError();
    }

    public static class Factory implements Data.Factory<Clock> {
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
