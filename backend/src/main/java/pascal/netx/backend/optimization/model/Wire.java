package pascal.netx.backend.optimization.model;

public final class Wire extends AbstractVertex {
    private final String label;

    public Wire(int index, String label) {
        super(index);
        this.label = label;
    }

    public String label() {
        return label;
    }

    @Override
    public String name() {
        return label;
    }
}
