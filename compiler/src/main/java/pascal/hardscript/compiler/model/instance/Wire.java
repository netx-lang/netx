package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.util.lang.Identifier;

import java.util.function.Supplier;

public final class Wire extends Data {
    private InstanceType type;

    public Wire(Identifier id, InstanceType type, int index) {
        super(id, index);
        this.type = type;
    }

    public static Wire of(Identifier id, InstanceType width, int index) {
        return new Wire(id, width, index);
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
        return id.toString();
    }

    public static class Factory extends Data.Factory<Wire> {
        private final InstanceType type;

        private Factory(Supplier<Integer> indexer, InstanceType type) {
            super(indexer);
            this.type = type;
        }

        public static Factory of(Supplier<Integer> indexer, InstanceType type) {
            return new Factory(indexer, type);
        }

        @Override
        public Wire create(Identifier id) {
            return new Wire(id, type, indexer.get());
        }
    }
}
