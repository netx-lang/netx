package pascal.hardscript.compiler.model.data.selector;

import pascal.hardscript.compiler.model.data.IntegerValue;

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
}
