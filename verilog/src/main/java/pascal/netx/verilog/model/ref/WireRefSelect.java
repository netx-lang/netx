package pascal.netx.verilog.model.ref;

import pascal.netx.util.hson.selector.Selector;

public record WireRefSelect(WireRef base, Selector selector) implements DataRef {

    public static WireRefSelect of(WireRef base, Selector selector) {
        return new WireRefSelect(base, selector);
    }

}
