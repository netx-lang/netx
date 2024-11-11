package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.hson.util.selector.Selector;

public record DataSelect(Data base, Selector selector) implements Data {

    public static DataSelect of(Data base, Selector selector) {
        return new DataSelect(base, selector);
    }

}
