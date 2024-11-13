package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.BitType;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

import java.util.function.Supplier;

public final class Clock extends Data {
    public Clock(Identifier id, int index) {
        super(id, index);
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

        protected Factory(Supplier<Integer> indexer) {
            super(indexer);
        }

        public static Factory of(Supplier<Integer> indexer) {
            return new Factory(indexer);
        }

        @Override
        public Clock create(Identifier id) {
            return new Clock(id, indexer.get());
        }
    }
}
