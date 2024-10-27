package pascal.hardscript.compiler.model.components.selector;

import pascal.hardscript.compiler.model.value.IntegerValue;

public record PartSelector(IntegerValue start, IntegerValue end) implements Selector {

    public static PartSelector of(IntegerValue start, IntegerValue end) {
        return new PartSelector(start, end);
    }
}
