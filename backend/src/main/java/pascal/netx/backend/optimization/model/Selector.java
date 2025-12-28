package pascal.netx.backend.optimization.model;

import javax.annotation.Nonnull;

public record Selector(int msb, int lsb) implements Comparable<Selector> {
    public int width() {
        return msb - lsb + 1;
    }

    @Override
    public @Nonnull String toString() {
        if (msb == lsb) {
            return "[" + msb + "]";
        }
        return "[%d:%d]".formatted(msb, lsb);
    }

    @Override
    public int compareTo(Selector selector) {
        if (this.msb != selector.msb) {
            return Integer.compare(this.msb, selector.msb);
        }
        return Integer.compare(this.lsb, selector.lsb);
    }
}
