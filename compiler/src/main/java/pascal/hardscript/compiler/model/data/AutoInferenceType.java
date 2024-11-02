package pascal.hardscript.compiler.model.data;

public final class AutoInferenceType implements DataType {
    private static final AutoInferenceType INSTANCE = new AutoInferenceType();

    private AutoInferenceType() {

    }

    public static AutoInferenceType getInstance() {
        return INSTANCE;
    }
}
