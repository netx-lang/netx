package pascal.hardscript.compiler.model.value;

import pascal.hardscript.compiler.model.circuit.selector.BitSelector;
import pascal.hardscript.compiler.model.circuit.selector.PartSelector;
import pascal.hardscript.compiler.model.circuit.selector.Selector;

import java.util.List;

public record ListValue(List<Value> values) implements Value {

    public Value select(Selector selector) {
        return switch (selector) {
            case BitSelector bitSelector -> values.get(bitSelector.index().value());
            case PartSelector partSelector -> {
                int start = partSelector.start().value();
                int end = partSelector.end().value();
                yield ListValue.of(values.subList(
                        Integer.min(start, end), Integer.max(start, end) + 1));
            }
        };
    }


    public static ListValue of(List<Value> values) {
        return new ListValue(values);
    }
}
