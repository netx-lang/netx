package pascal.netx.simulator.model;

import pascal.netx.util.lang.Identifier;

public final class ComputationNode extends Node {
    private ComputationNode(Identifier id) {
        super(id);
    }

    public static ComputationNode of(Identifier id) {
        return new ComputationNode(id);
    }
}
