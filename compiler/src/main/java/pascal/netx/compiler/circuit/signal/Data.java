package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.util.lang.Identifier;

public sealed interface Data extends Signal permits Access, Composition, Wire {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends Data> implements Entity {
        protected final NXCompiler compiler;

        protected Factory(NXCompiler compiler) {
            this.compiler = compiler;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public Type getType();
    }
}
