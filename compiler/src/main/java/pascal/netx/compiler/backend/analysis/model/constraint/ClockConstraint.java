package pascal.netx.compiler.backend.analysis.model.constraint;

import pascal.netx.compiler.model.circuit.signal.Clock;
import pascal.netx.compiler.model.circuit.signal.Signal;

public final class ClockConstraint extends UnaryConstraint {

    public ClockConstraint(Signal inst) {
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
