package pascal.netx.compiler.model.variable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.io.NXCompiler;
import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.variable.type.InterfaceType;
import pascal.netx.compiler.model.variable.type.Type;
import pascal.netx.util.lang.Identifier;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Objects;

public final class Bundle extends Composition {
    private static final Logger log = LoggerFactory.getLogger(Bundle.class);
    private final LinkedHashMap<Identifier, Mutable> map = new LinkedHashMap<>();

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
            log.error("{}: Field {} not found in bundle {}", field.getSourceLoc(), field, this);
            throw new CompileCancellationException();
        }
        return map.get(field);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Bundle that = (Bundle) o;
        return map.values().equals(that.map.values());
    }

    @Override
    public int hashCode() {
        return Objects.hash(map.values());
    }

    @Override
    public Collection<Mutable> getElements() {
        return map.values();
    }

    public static class Factory extends Mutable.Factory<Bundle> {
        private final LinkedHashMap<Identifier, Mutable.Factory<?>> factories;
        private final InterfaceType type;

        public Factory(Identifier id, NXCompiler compiler,
                       LinkedHashMap<Identifier, Mutable.Factory<?>> factories) {
            super(compiler);
            this.factories = factories;
            this.type = InterfaceType.of(id,
                    factories.values().stream().map(Mutable.Factory::getType).toList());
        }

        @Override
        public Bundle create(ComponentScope parent, Identifier id) {
            var result = new Bundle(id, type, compiler.nextDataIndex());

            for (var entry : factories.entrySet()) {
                Identifier fieldID = entry.getKey();
                Mutable.Factory<?> factory = entry.getValue();

                Identifier dataID = Identifier.of(id.name() + "." + fieldID.name()).setSourceLoc(id.getSourceLoc());
                Mutable data = factory.create(parent, dataID);
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
