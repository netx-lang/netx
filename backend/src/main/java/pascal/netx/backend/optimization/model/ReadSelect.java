package pascal.netx.backend.optimization.model;

public final class ReadSelect extends AbstractVertex {
    private final Selector selector;

    public ReadSelect(int index, Selector selector) {
        super(index);
        this.selector = selector;
    }

    public int msb() {
        return selector.msb();
    }

    public int lsb() {
        return selector.lsb();
    }

    @Override
    public String name() {
        return selector.toString();
    }
}
