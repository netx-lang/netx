package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.util.lang.Identifier;

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

        public Factory(InstanceType type, PackageScope module) {
            super(module);
            this.type = type;
        }

        @Override
        public Wire create(ComponentScope parent, Identifier id) {
            return new Wire(id, parent, type, pack.getDataIndexer().get());
        }

        @Override
        public InstanceType getType() {
            return type;
        }
    }
}
