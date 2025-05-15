package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.model.variable.Clock;
import pascal.netx.compiler.model.variable.Variable;

public final class ClockConstraint extends UnaryConstraint {

    public ClockConstraint(Variable inst) {
        super(inst);
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
