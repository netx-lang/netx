package pascal.netx.compiler.model.variable;

import pascal.netx.compiler.elaborator.Logger;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.type.BitType;
import pascal.netx.compiler.model.variable.type.Type;
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
