package pascal.hardscript.compiler.model.instance.selector;

import pascal.hardscript.compiler.exception.CompileCancellationException;
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
}
