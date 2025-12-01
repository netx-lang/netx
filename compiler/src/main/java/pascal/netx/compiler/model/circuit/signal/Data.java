package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.type.Type;
import pascal.netx.util.lang.Identifier;

public sealed interface Data extends Signal permits Access, Composition, Wire {
    int getIndex();

    Identifier getIdentifier();

    ComponentScope getParent();

    abstract class Factory<T extends Data> {
        protected final NXCompiler.WireIndexer indexer;

        protected Factory(NXCompiler.WireIndexer indexer) {
            this.indexer = indexer;
        }

        abstract public T create(ComponentScope parent, Identifier id);

        abstract public Type getType();
    }
}
