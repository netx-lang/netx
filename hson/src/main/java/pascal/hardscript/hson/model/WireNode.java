package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class WireNode extends DataNode {
    public WireNode(Identifier id, int width) {
        super(id, width);
    }

    public static WireNode of(Identifier identifier, int width) {
        return new WireNode(identifier, width);
    }
}
