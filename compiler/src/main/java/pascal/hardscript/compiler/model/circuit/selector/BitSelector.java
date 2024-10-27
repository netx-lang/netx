package pascal.hardscript.compiler.model.circuit.selector;

import pascal.hardscript.compiler.model.value.IntegerValue;

public record BitSelector(IntegerValue index) implements Selector {

    public static BitSelector of(IntegerValue index) {
        return new BitSelector(index);
    }
}
