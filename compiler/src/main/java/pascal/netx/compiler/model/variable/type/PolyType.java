package pascal.netx.compiler.model.variable.type;

public final class PolyType implements Type {
    private static final PolyType INSTANCE = new PolyType();

    private PolyType() {

    }

    public static PolyType getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
