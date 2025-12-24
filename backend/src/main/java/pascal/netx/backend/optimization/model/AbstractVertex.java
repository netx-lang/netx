package pascal.netx.backend.optimization.model;

import java.util.Objects;

public sealed abstract class AbstractVertex implements Vertex permits Component, Constant, Wire {
    protected final int index;

    public AbstractVertex(int index) {
        this.index = index;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractVertex vertex = (AbstractVertex) o;
        return index == vertex.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }

    @Override
    public int index() {
        return index;
    }

    @Override
    abstract public String name();
}
