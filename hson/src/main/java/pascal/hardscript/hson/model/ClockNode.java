package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record ClockNode(Identifier id) implements StreamNode {
    public static ClockNode of(Identifier id) {
        return new ClockNode(id);
    }

    @Override
    public Identifier getIdentifier() {
        return id;
    }
}
