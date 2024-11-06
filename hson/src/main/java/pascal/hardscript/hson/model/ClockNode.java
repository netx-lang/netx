package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public record ClockNode(Identifier id) implements StatefulNode {
    public static ClockNode of(Identifier id) {
        return new ClockNode(id);
    }
}
