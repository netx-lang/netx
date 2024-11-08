package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class ClockNode extends DataNode implements StreamNode {
    private final Identifier id;

    public ClockNode(Identifier id) {
        this.id = id;
    }

    public static ClockNode of(Identifier id) {
        return new ClockNode(id);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
