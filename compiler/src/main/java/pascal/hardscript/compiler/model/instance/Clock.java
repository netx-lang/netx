package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.GlobalScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

public final class Clock extends Data {
    public Clock(Identifier id, ComponentScope parent, int index) {
        super(id, parent, index);
    }

    @Override
    public InstanceType getType() {
        return BitType.forClock();
    }

    @Override
    public void setType(InstanceType type) {
        throw new ShallNotReachHereError();
    }

    public static class Factory extends Data.Factory<Clock> {

        public Factory(GlobalScope global) {
            super(global);
        }

        @Override
        public Clock create(ComponentScope parent, Identifier id) {
            return new Clock(id, parent, global.getDataIndexer().get());
        }

        @Override
        public InstanceType getType() {
            return BitType.forClock();
        }
    }
}
