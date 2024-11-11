package pascal.hardscript.compiler.model.instance.selector;

import pascal.hardscript.compiler.exception.CompileCancellationException;
import pascal.hardscript.compiler.model.instance.Int32Value;
import pascal.hardscript.compiler.model.instance.IntegerValue;

public record PartSelector(IntegerValue start, IntegerValue end) implements Selector {

    public static PartSelector of(IntegerValue start, IntegerValue end) {
        return new PartSelector(start, end);
    }

    @Override
    public String toString() {
        return String.format("[%d : %d]", start.toInt(), end.toInt());
    }

    @Override
    public int getWidth() {
        return Math.abs(end.toInt() - start.toInt()) + 1;
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
        return BitSelector.of(Int32Value.of(index + lsb));
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
        return PartSelector.of(Int32Value.of(resF), Int32Value.of(resT));
    }
}
