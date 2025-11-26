package pascal.netx.compiler.circuit.signal.selector;

import pascal.netx.compiler.circuit.EList;
import pascal.netx.compiler.circuit.Entity;
import pascal.netx.compiler.utils.CompileCancellationException;

import javax.annotation.Nonnull;
import java.util.Objects;

public record PartSelector(int start, int end) implements Selector {

    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d:%d]", start, end);
    }

    @Override
    public int getWidth() {
        return Math.abs(end - start) + 1;
    }

    @Override
    public int msb() {
        return Math.max(start, end);
    }

    @Override
    public int lsb() {
        return Math.min(start, end);
    }

    @Override
    public Selector refine(Selector selector) {
        return switch (selector) {
            case BitSelector(var value) -> refineBitSelector(value);
            case PartSelector(var fv, var tv) -> refinePartSelector(fv, tv);
        };
    }

    @Override
    public Entity applyOn(EList list) {
        return EList.of(list.subList(Integer.min(start, end), Integer.max(start, end) + 1));
    }

    private BitSelector refineBitSelector(int index) {
        if (lsb() + index > msb()) {
            throw new CompileCancellationException();
        }
        return new BitSelector(index + lsb());
    }

    private PartSelector refinePartSelector(int f, int t) {
        int s = start, e = end;
        if ((s > e) != (f > t)) {
            throw new CompileCancellationException();
        }
        int resF = s > e ? e + f : s + f;
        int resT = s > e ? e + t : s + t;
        if ((s > e && resF > s) || (s <= e && resT > e)) {
            throw new CompileCancellationException();
        }
        return new PartSelector(resF, resT);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        PartSelector that = (PartSelector) o;
        return Objects.equals(end, that.end) && Objects.equals(start, that.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }
}
