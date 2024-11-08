package pascal.hardscript.hson.model;

import pascal.hardscript.util.lang.Identifier;

public abstract class Node implements Comparable<Node> {
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

    public enum Type {
        COMPONENT, CLOCK, WIRE
    }

    @Override
    public int compareTo(Node that) {
        return Integer.compare(this.order, that.order);
    }
}
