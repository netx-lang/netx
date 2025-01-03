package pascal.netx.verilog.model;

import pascal.netx.util.lang.Identifier;

public record Wire(Identifier id, int width) {
    public static Wire of(Identifier identifier, int width) {
        return new Wire(identifier, width);
    }
}
