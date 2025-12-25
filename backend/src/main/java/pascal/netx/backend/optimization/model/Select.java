package pascal.netx.backend.optimization.model;

public final class Select extends AbstractVertex {
    private final int msb;
    private final int lsb;

    public Select(int index, int msb, int lsb) {
        super(index);
        this.msb = msb;
        this.lsb = lsb;
    }

    public int msb() {
        return msb;
    }

    public int lsb() {
        return lsb;
    }

    @Override
    public String name() {
        if (msb == lsb) {
            return "[" + msb + "]";
        }
        return "[%d:%d]".formatted(msb, lsb);
    }
}
