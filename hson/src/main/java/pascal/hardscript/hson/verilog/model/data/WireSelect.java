package pascal.hardscript.hson.verilog.model.data;

import pascal.hardscript.hson.util.selector.Selector;

public record WireSelect(Wire base, Selector selector) implements Data {
    public static WireSelect of(Wire base, Selector selector) {
        return new WireSelect(base, selector);
    }
}
