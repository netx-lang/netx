package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.util.lang.Identifier;

import java.util.function.Supplier;

public final class Wire extends Data {
    private InstanceType type;

    public Wire(Identifier id, ComponentScope parent, InstanceType type, int index) {
        super(id, parent, index);
        this.type = type;
    }

    public static Wire of(Identifier id, ComponentScope parent, InstanceType width, int index) {
        return new Wire(id, parent, width, index);
    }

    @Override
    public InstanceType getType() {
        return type;
    }

    @Override
    public void setType(InstanceType type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return id.toStringWithLoc();
    }

    public static class Factory extends Data.Factory<Wire> {
        private final InstanceType type;

        public Factory(InstanceType type, Supplier<Integer> indexer) {
            super(indexer);
            this.type = type;
        }

        @Override
        public Wire create(ComponentScope parent, Identifier id) {
            return new Wire(id, parent, type, indexer.get());
        }
    }
}
