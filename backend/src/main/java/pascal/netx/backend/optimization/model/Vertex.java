package pascal.netx.backend.optimization.model;

import java.util.Objects;

public record Vertex(int index, String label) {
    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Vertex vertex = (Vertex) o;
        return index == vertex.index;
    }

    @Override
    public int hashCode() {
        return Objects.hash(index);
    }
}
