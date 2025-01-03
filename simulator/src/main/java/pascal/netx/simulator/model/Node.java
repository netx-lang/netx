package pascal.netx.simulator.model;

import pascal.netx.util.lang.Identifier;

public abstract sealed class Node implements Comparable<Node> permits ComputationNode, DataNode {
    private final Identifier id;
    private int order;

    protected Node(Identifier id) {
        this.id = id;
    }

    public Identifier getIdentifier() {
        return id;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    public int compareTo(Node that) {
        return Integer.compare(this.order, that.order);
    }
}
