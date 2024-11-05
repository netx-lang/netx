package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.instance.Clock;
import pascal.hardscript.compiler.model.instance.Instance;

public final class ClockConstraint extends UnaryConstraint {

    private ClockConstraint(Instance inst) {
        super(inst);
    }

    public static ClockConstraint of(Instance inst) {
        return new ClockConstraint(inst);
    }

    @Override
    public boolean holds() {
        return inst instanceof Clock;
    }

    @Override
    public String error() {
        return String.format("Expected clock type, but got %s", inst);
    }
}
