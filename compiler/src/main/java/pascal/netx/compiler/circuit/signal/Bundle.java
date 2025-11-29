package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.NXCompiler;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.signal.type.InterfaceType;
import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.compiler.utils.CompileCancellationException;
import pascal.netx.compiler.utils.Logger;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class Bundle extends Composition {
    private final LinkedHashMap<Identifier, Data> map = new LinkedHashMap<>();

    private Bundle(Identifier id, InterfaceType type, int index) {
        super(type, id, index);
    }

    @Override
    public InterfaceType getType() {
        return (InterfaceType) super.getType();
    }

    @Override
    public ComponentScope getParent() {
        return map.values().stream().findAny().orElseThrow().getParent();
    }

    @Nonnull
    public Entity getField(Identifier field) {
        if (!map.containsKey(field)) {
            Logger.error(field.getSourceLoc(), "Field {} not found in bundle {}", field, this);
            throw new CompileCancellationException();
        }
        return map.get(field);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Bundle bundle = (Bundle) o;
        return Objects.equals(map, bundle.map);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), map);
    }

    @Override
    public Collection<Data> getElements() {
        return map.values();
    }

    public LinkedHashMap<Identifier, Data> getFields() {
        return map;
    }

    public static class Factory extends Data.Factory<Bundle> {
        private final LinkedHashMap<Identifier, Data.Factory<?>> factories;
        private final InterfaceType type;

        public Factory(Identifier id, NXCompiler.WireIndexer indexer,
                       LinkedHashMap<Identifier, Data.Factory<?>> factories) {
            super(indexer);
            this.factories = factories;
            this.type = InterfaceType.of(id,
                    factories.values().stream().map(Data.Factory::getType).toList());
        }

        @Override
        public Bundle create(ComponentScope parent, Identifier id) {
            var result = new Bundle(id, type, indexer.next());

            for (var entry : factories.entrySet()) {
                Identifier fieldID = entry.getKey();
                Data.Factory<?> factory = entry.getValue();

                Identifier dataID = Identifier.of(id.name() + "." + fieldID.name()).setSourceLoc(id.getSourceLoc());
                Data data = factory.create(parent, dataID);
                result.map.put(fieldID, data);
                parent.bind(dataID, data);
            }
            return result;
        }

        @Override
        public Type getType() {
            return type;
        }
    }
}
