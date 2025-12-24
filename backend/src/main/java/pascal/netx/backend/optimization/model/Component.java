package pascal.netx.backend.optimization.model;

public final class Component extends AbstractVertex {
    private final Label label;

    public Component(int index, String label) {
        super(index);
        this.label = Label.valueOf(label.toUpperCase());
    }

    public Component(int index, Label label) {
        super(index);
        this.label = label;
    }

    public Label label() {
        return label;
    }

    @Override
    public String name() {
        return label.name();
    }

    public enum Label {
        REGISTER, ASYNC_REGISTER,
        CONCAT, OR, NOT, AND, XOR, NOR, NAND, XNOR,
        EQ, NE, SLT, SLE, SGT, SGE, ULT, ULE, UGT, UGE,
        ADD, SUB, SHL, MUL, DIV, MOD, ASHR, LSHR;
    }
}
