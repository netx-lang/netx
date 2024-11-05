package pascal.hardscript.compiler.model.instance.selector;

import pascal.hardscript.compiler.model.instance.IntegerValue;

public record BitSelector(IntegerValue index) implements Selector {

    public static BitSelector of(IntegerValue index) {
        return new BitSelector(index);
    }

    @Override
    public String toString() {
        return String.format("[%d]", index.toInt());
    }

    @Override
    public int getWidth() {
        return 1;
    }
}
