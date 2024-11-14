package pascal.hardscript.hson.verilog.model.ref;

import pascal.hardscript.hson.util.selector.Selector;

public record DataRefSelect(DataRef base, Selector selector) implements DataRef {

    public static DataRefSelect of(DataRef base, Selector selector) {
        return new DataRefSelect(base, selector);
    }

}
