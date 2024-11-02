package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.data.Clock;
import pascal.hardscript.compiler.model.data.Data;

public final class ClockConstraint extends UnaryConstraint {

    private ClockConstraint(Data data) {
        super(data);
    }

    public static ClockConstraint of(Data data) {
        return new ClockConstraint(data);
    }

    @Override
    public boolean holds() {
        return data instanceof Clock;
    }

    @Override
    public String error() {
        return String.format("Expected clock type, but got %s", data);
    }
}
