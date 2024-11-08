package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public class ComputationNode extends Node {
    private ComputationNode(Identifier id) {
        super(id);
    }

    public static ComputationNode of(Identifier id) {
        return new ComputationNode(id);
    }
}
