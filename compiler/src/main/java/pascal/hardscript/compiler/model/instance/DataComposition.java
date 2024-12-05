package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

public abstract sealed class DataComposition implements MutableInstance permits Array, DataBundle {
    protected final InstanceType type;
    protected final Identifier id;
    protected final int index;

    protected DataComposition(InstanceType type, Identifier id, int index) {
        this.type = type;
        this.id = id;
        this.index = index;
    }

    abstract public Collection<MutableInstance> getElements();

    public List<Data> flatten() {
        List<Data> result = new LinkedList<>();
        for (var value : getElements()) {
            switch (value) {
                case Data data -> result.add(data);
                case DataComposition composition -> result.addAll(composition.flatten());
                default -> throw new ShallNotReachHereError();
            }
        }
        return result;
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

    @Override
    public int getIndex() {
        return index;
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    @Override
    public String toString() {
        return id.toStringWithLoc();
    }
}
