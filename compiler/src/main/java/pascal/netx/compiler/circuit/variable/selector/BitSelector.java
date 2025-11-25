package pascal.netx.compiler.circuit.variable.selector;

import pascal.netx.compiler.circuit.variable.IntegerValue;
import pascal.netx.compiler.utils.CompileCancellationException;

import javax.annotation.Nonnull;
import java.util.Objects;

public record BitSelector(IntegerValue index) implements Selector {

    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d]", index.toInt());
    }

    @Override
    public int getWidth() {
        return 1;
    }

    @Override
    public IntegerValue msb() {
        return index;
    }

    @Override
    public IntegerValue lsb() {
        return index;
    }

    @Override
    public Selector refine(Selector selector) {
        switch (selector) {
            case BitSelector(var value) -> {
                if (value.toInt() != 0) {
                    throw new CompileCancellationException();
                }
            }
            case PartSelector(var f, var t) -> {
                if (f.toInt() != 0 || t.toInt() != 0) {
                    throw new CompileCancellationException();
                }
            }
        }
        return this;
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
