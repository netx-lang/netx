package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.variable.type.BitType;
import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.Identifier;

public final class Wire extends Data {
    private Type type;

    public Wire(Identifier id, ComponentScope parent, Type type, int index) {
        super(id, parent, index);
        this.type = type;
    }

    public static Wire of(Identifier id, ComponentScope parent, Type width, int index) {
        return new Wire(id, parent, width, index);
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return id.toStringWithLoc();
    }

    public static class Factory extends Data.Factory<Wire> {
        private final Type type;

        public Factory(Type type, NXCompiler compiler) {
            super(compiler);
            if (type instanceof BitType(int width) && width == 0) {
                Logger.error("Cannot create a wire with zero width");
                throw new CompileCancellationException();
            }
            this.type = type;
        }

        @Override
        public Wire create(ComponentScope parent, Identifier id) {
            return new Wire(id, parent, type, compiler.nextDataIndex());
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
