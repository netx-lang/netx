package pascal.netx.compiler.model.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.instance.type.InterfaceType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.GlobalScope;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class DataBundle extends DataComposition {
    private static final Logger log = LoggerFactory.getLogger(DataBundle.class);
    private final LinkedHashMap<Identifier, MutableInstance> map = new LinkedHashMap<>();

    private DataBundle(Identifier id, InterfaceType type, int index) {
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
            log.error("{}: Field {} not found in bundle {}", field.getSourceLoc(), field, this);
            throw new CompileCancellationException();
        }
        return map.get(field);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        DataBundle that = (DataBundle) o;
        return map.values().equals(that.map.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(map.values());
    }

    @Override
    public Collection<MutableInstance> getElements() {
        return map.values();
    }

    public static class Factory extends MutableInstance.Factory<DataBundle> {
        private final LinkedHashMap<Identifier, MutableInstance.Factory<?>> factories;
        private final InterfaceType type;

        public Factory(Identifier id, GlobalScope global,
                       LinkedHashMap<Identifier, MutableInstance.Factory<?>> factories) {
            super(global);
            this.factories = factories;
            this.type = InterfaceType.of(id,
                    factories.values().stream().map(MutableInstance.Factory::getType).toList());
        }

        @Override
        public DataBundle create(ComponentScope parent, Identifier id) {
            var result = new DataBundle(id, type, global.getDataIndexer().get());

            for (var entry : factories.entrySet()) {
                Identifier fieldID = entry.getKey();
                MutableInstance.Factory<?> factory = entry.getValue();

                Identifier dataID = Identifier.of(id.name() + "." + fieldID.name()).setSourceLoc(id.getSourceLoc());
                MutableInstance data = factory.create(parent, dataID);
                result.map.put(fieldID, data);
                parent.bind(dataID, data);
            }
            return result;
        }

        @Override
        public InstanceType getType() {
            return type;
        }
    }
}
