package pascal.netx.backend.optimization.model;

public final class Wire extends AbstractVertex {
    private final String label;
    private final int width;

    public Wire(int index, String label, int width) {
        super(index);
        this.label = label;
        this.width = width;
    }

    public String label() {
        return label;
    }

    @Override
    public int outWidth() {
        return width;
    }

    @Override
    public String name() {
        return label;
    }
}
