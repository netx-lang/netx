package pascal.netx.backend.optimization.model;

public final class WriteSelect extends AbstractVertex {
    private final String label;
    private final Selector selector;

    public WriteSelect(int index, String label, Selector selector) {
        super(index);
        this.label = label;
        this.selector = selector;
    }

    public String label() {
        return label;
    }

    public Selector selector() {
        return selector;
    }

    @Override
    public int outWidth() {
        return selector.width();
    }

    @Override
    public String name() {
        return label + selector.toString();
    }
}
