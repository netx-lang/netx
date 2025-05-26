package pascal.netx.compiler.model.variable.selector;

import pascal.netx.compiler.exception.CompileCancellationException;
import pascal.netx.compiler.model.variable.Int32Value;
import pascal.netx.compiler.model.variable.IntegerValue;

import javax.annotation.Nonnull;
import java.util.Objects;

public record PartSelector(IntegerValue start, IntegerValue end) implements Selector {

    @Override
    @Nonnull
    public String toString() {
        return String.format("[%d:%d]", start.toInt(), end.toInt());
    }

    @Override
    public int getWidth() {
        return Math.abs(end.toInt() - start.toInt()) + 1;
    }

    @Override
    public IntegerValue msb() {
        return start;
    }

    @Override
    public IntegerValue lsb() {
        return end;
    }

    @Override
    public Selector refine(Selector selector) {
        return switch (selector) {
            case BitSelector(var value) -> refineBitSelector(value.toInt());
            case PartSelector(var fv, var tv) -> refinePartSelector(fv.toInt(), tv.toInt());
        };
    }

    private BitSelector refineBitSelector(int index) {
        int msb = Math.max(start.toInt(), end.toInt());
        int lsb = Math.min(start.toInt(), end.toInt());
        if (lsb + index > msb) {
            throw new CompileCancellationException();
        }
        return new BitSelector(Int32Value.of(index + lsb));
    }

    private PartSelector refinePartSelector(int f, int t) {
        int s = start.toInt(), e = end.toInt();
        if ((s > e) != (f > t)) {
            throw new CompileCancellationException();
        }
        int resF = s > e ? e + f : s + f;
        int resT = s > e ? e + t : s + t;
        if ((s > e && resF > s) || (s <= e && resT > e)) {
            throw new CompileCancellationException();
        }
        IntegerValue start1 = Int32Value.of(resF);
        return new PartSelector(start1, Int32Value.of(resT));
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
