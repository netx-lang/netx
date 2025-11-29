package pascal.netx.compiler.backend.analysis.model.constraint;

import pascal.netx.compiler.circuit.signal.Signal;

public abstract sealed class UnaryConstraint implements WireConstraint permits ClockConstraint {
    protected final Signal inst;

    protected UnaryConstraint(Signal inst) {
        this.inst = inst;
    }

    public Signal getData() {
        return inst;
    }
}
