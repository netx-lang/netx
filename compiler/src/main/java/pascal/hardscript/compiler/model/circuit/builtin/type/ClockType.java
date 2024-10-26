package pascal.hardscript.compiler.model.circuit.builtin.type;

public final class ClockType implements ComponentType {
    private static final ClockType INSTANCE = new ClockType();

    private ClockType() {
    }

    public static ClockType getInstance() {
        return INSTANCE;
    }
}
