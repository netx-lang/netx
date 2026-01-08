package pascal.netx.backend.optimization.model;

public final class ReadSelect extends Component {
    private final Selector selector;

    public ReadSelect(int index, Selector selector) {
        super(index, Label.SELECT, selector.width());
        this.selector = selector;
    }

    public Selector selector() {
        return selector;
    }

    @Override
    public String name() {
        return selector.toString();
    }
}
