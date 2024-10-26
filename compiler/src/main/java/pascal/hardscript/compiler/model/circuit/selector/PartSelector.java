package pascal.hardscript.compiler.model.circuit.selector;

import pascal.hardscript.compiler.model.value.IntValue;

public record PartSelector(IntValue start, IntValue end) implements Selector {

    public static PartSelector of(IntValue start, IntValue end) {
        return new PartSelector(start, end);
    }
}
