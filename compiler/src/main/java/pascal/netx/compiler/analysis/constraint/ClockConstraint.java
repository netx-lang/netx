package pascal.netx.compiler.analysis.constraint;

import pascal.netx.compiler.model.instance.Clock;
import pascal.netx.compiler.model.instance.Instance;

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
