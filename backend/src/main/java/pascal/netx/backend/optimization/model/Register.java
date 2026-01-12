package pascal.netx.backend.optimization.model;

public final class Register extends Component {
    private final boolean isPosEdge;

    public Register(int index, int width, boolean isPosEdge) {
        super(index, Label.REGISTER, width);
        this.isPosEdge = isPosEdge;
    }

    public boolean isPosEdge() {
        return isPosEdge;
    }
}
