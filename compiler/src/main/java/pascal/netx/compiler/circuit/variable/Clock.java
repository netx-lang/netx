package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.variable.type.BitType;
import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

public final class Clock extends Data {
    public Clock(Identifier id, ComponentScope parent, int index) {
        super(id, parent, index);
    }

    @Override
    public String toString() {
        return id.toStringWithLoc();
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

        public Factory(NXCompiler compiler) {
            super(compiler);
        }

        @Override
        public Clock create(ComponentScope parent, Identifier id) {
            return new Clock(id, parent, compiler.nextDataIndex());
        }

        @Override
        public Type getType() {
            return BitType.forClock();
        }
    }
}
