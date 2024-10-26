package pascal.hardscript.compiler.model.circuit.selector;

import pascal.hardscript.compiler.model.value.IntValue;

public record BitSelector(IntValue index) implements Selector {

    public static BitSelector of(IntValue index) {
        return new BitSelector(index);
    }
}
