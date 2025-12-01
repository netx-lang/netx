package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.type.BitType;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

public final class Clock extends Wire {
    public Clock(Identifier id, ComponentScope parent, int index) {
        super(id, parent, BitType.forClock(), index);
    }

    @Override
    public Type getType() {
        return BitType.forClock();
    }

    @Override
    public void setType(Type type) {
        throw new ShallNotReachHereError();
    }

    public static class Factory extends Data.Factory<Clock> {

        public Factory(NXCompiler.WireIndexer indexer) {
            super(indexer);
        }

        @Override
        public Clock create(ComponentScope parent, Identifier id) {
            return new Clock(id, parent, indexer.next());
        }

        @Override
        public Type getType() {
            return BitType.forClock();
        }
    }
}
