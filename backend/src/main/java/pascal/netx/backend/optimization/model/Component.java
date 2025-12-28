package pascal.netx.backend.optimization.model;

public final class Component extends AbstractVertex {
    private final Label label;
    private final int width;

    public Component(int index, String label, int width) {
        super(index);
        this.label = Label.valueOf(label.toUpperCase());
        this.width = width;
    }

    public Component(int index, Label label, int width) {
        super(index);
        this.label = label;
        this.width = width;
    }

    public Label label() {
        return label;
    }

    @Override
    public int outWidth() {
        return width;
    }

    @Override
    public String name() {
        return label.name();
    }

    public enum Label {
        REGISTER, ASYNC_REGISTER,
        CAST, UCAST, TO,
        CONCAT, OR, NOT, AND, XOR, NOR, NAND, XNOR,
        EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
        ADD, SUB, SHL, MUL, DIV, MOD, ASHR, LSHR
    }
}
