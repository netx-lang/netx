package pascal.hardscript.hson.verilog.model.ref;

import pascal.hardscript.hson.util.selector.Selector;

public record WireRefSelect(WireRef base, Selector selector) implements DataRef {

    public static WireRefSelect of(WireRef base, Selector selector) {
        return new WireRefSelect(base, selector);
    }

}
