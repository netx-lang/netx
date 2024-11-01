package pascal.hardscript.compiler.model.data;

public final class ImplicitType implements DataType {
    private static final ImplicitType INSTANCE = new ImplicitType();

    private ImplicitType() {

    }

    public static ImplicitType getInstance() {
        return INSTANCE;
    }
}
