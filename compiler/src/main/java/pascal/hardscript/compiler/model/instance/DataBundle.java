package pascal.hardscript.compiler.model.instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.InterfaceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class DataBundle implements MutableInstance {
    private static final Logger log = LoggerFactory.getLogger(DataBundle.class);
    private final LinkedHashMap<Identifier, Data> map = new LinkedHashMap<>();
    private final InstanceType type;
    private final Identifier id;
    private final int index;

    private DataBundle(Identifier id, InstanceType type, int index) {
        this.id = id;
        this.type = type;
        this.index = index;
    }

    @Override
    public InstanceType getType() {
        return type;
    }

    @Override
    public void setType(InstanceType type) {
        throw new ShallNotReachHereError();
    }

    @Override
    public boolean isBuiltin() {
        return false;
    }

    public List<Data> flatten() {
        return new ArrayList<>(map.values());
    }

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
    public String toString() {
        return id.toStringWithLoc();
    }

    @Override
    public int getIndex() {
        return index;
    }

    public static class Factory implements Instance.Factory<DataBundle> {
        private final LinkedHashMap<Identifier, Data.Factory<?>> factories;
        private final Supplier<Integer> indexer;
        private final InterfaceType type;

        public Factory(LinkedHashMap<Identifier, Data.Factory<?>> factories, InterfaceType type, Supplier<Integer> indexer) {
            this.factories = factories;
            this.type = type;
            this.indexer = indexer;
        }

        @Override
        public DataBundle create(ComponentScope parent, Identifier id) {
            var result = new DataBundle(id, type, indexer.get());

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
    }
}
