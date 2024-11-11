package pascal.hardscript.hson.verilog.model;

import pascal.hardscript.util.lang.Identifier;

public record Wire(Identifier id, int width) implements Data {
    public static Wire of(Identifier identifier, int width) {
        return new Wire(identifier, width);
    }

}
