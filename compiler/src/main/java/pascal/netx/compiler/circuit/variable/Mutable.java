package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.util.lang.Identifier;

public sealed interface Mutable extends Variable permits Data, Access, Composition {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends Mutable> implements Entity {
        protected final NXCompiler compiler;

        protected Factory(NXCompiler compiler) {
            this.compiler = compiler;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public Type getType();
    }
}
