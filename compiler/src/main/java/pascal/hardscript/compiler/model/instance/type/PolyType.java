package pascal.hardscript.compiler.model.instance.type;

public final class PolyType implements InstanceType {
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
