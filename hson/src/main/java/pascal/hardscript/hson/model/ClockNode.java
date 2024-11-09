package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public final class ClockNode extends DataNode {
    public ClockNode(Identifier id) {
        super(id, 1);
    }

    public static ClockNode of(Identifier id) {
        return new ClockNode(id);
    }

    public boolean tick() {
        var next = getValue().not();
        setValue(next);
        return next.toBoolean();
    }
}
