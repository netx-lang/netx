package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.Identifier;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

public abstract sealed class Composition implements Mutable permits Array, Bundle {
    protected final Type type;
    protected final Identifier id;
    protected final int index;

    protected Composition(Type type, Identifier id, int index) {
        this.type = type;
        this.id = id;
        this.index = index;
    }

    abstract public Collection<Mutable> getElements();

    public List<Data> flatten() {
        List<Data> result = new LinkedList<>();
        for (var value : getElements()) {
            switch (value) {
                case Data data -> result.add(data);
                case Composition composition -> result.addAll(composition.flatten());
                default -> throw new ShallNotReachHereError();
            }
        }
        return result;
    }

    @Override
    public Type getType() {
        return type;
    }

    @Override
    public void setType(Type type) {
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

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Composition that = (Composition) o;
        return index == that.index && Objects.equals(type, that.type) && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id, index);
    }
}
