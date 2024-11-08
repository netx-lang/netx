package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

import java.util.Objects;

public final class WireNode extends DataNode implements Node {
    private final Identifier id;
    private final int width;

    public WireNode(Identifier id, int width) {
        this.id = id;
        this.width = width;
    }

    public static WireNode of(Identifier identifier, int width) {
        return new WireNode(identifier, width);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }

    public int getWidth() {
        return width;
    }
}
