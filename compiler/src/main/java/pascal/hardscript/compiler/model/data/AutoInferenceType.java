package pascal.hardscript.compiler.model.data;

public final class AutoInferenceType implements DataType {
    private static final AutoInferenceType INSTANCE = new AutoInferenceType();

    private AutoInferenceType() {

    }

    public static AutoInferenceType getInstance() {
        return INSTANCE;
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
