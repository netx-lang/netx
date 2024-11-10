package pascal.hardscript.hson.simulator.model;

import pascal.hardscript.util.lang.Identifier;

public final class ComputationNode extends Node {
    private ComputationNode(Identifier id) {
        super(id);
    }

    public static ComputationNode of(Identifier id) {
        return new ComputationNode(id);
    }
}
