package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.model.variable.Variable;

public abstract sealed class UnaryConstraint implements DataConstraint permits ClockConstraint {
    protected final Variable inst;

    protected UnaryConstraint(Variable inst) {
        this.inst = inst;
    }

    public Variable getData() {
        return inst;
    }
}
