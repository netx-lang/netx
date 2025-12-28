package pascal.netx.backend.optimization.model;

public final class ReadSelect extends AbstractVertex {
    private final Selector selector;

    public ReadSelect(int index, Selector selector) {
        super(index);
        this.selector = selector;
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
        return selector.toString();
    }
}
