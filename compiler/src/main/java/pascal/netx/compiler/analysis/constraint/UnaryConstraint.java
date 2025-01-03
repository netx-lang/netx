package pascal.netx.compiler.analysis.constraint;

import pascal.netx.compiler.model.instance.Instance;

public abstract sealed class UnaryConstraint implements DataConstraint permits ClockConstraint {
    protected final Instance inst;

    protected UnaryConstraint(Instance inst) {
        this.inst = inst;
    }

    public Instance getData() {
        return inst;
    }
}
