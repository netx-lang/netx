package pascal.netx.compiler.model.circuit.signal.selector;

import pascal.netx.compiler.model.circuit.EList;
import pascal.netx.compiler.model.circuit.Entity;
import pascal.netx.compiler.utils.CompileCancellationException;

import javax.annotation.Nonnull;
import java.util.Objects;

public record BitSelector(int index) implements Selector {
    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d]", index);
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public int msb() {
        return index;
    }

    @Override
    public int lsb() {
        return index;
    }

    @Override
    public Selector refine(Selector selector) {
        switch (selector) {
            case BitSelector(var value) -> {
                if (value != 0) {
                    throw new CompileCancellationException();
                }
            }
            case PartSelector(var f, var t) -> {
                if (f != 0 || t != 0) {
                    throw new CompileCancellationException();
                }
            }
        }
        return this;
    }

    @Override
    public Entity applyOn(EList list) {
        return list.get(index());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        BitSelector that = (BitSelector) o;
        return Objects.equals(index, that.index);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(index);
    }
}
