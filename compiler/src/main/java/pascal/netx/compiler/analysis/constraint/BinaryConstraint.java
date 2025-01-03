package pascal.netx.compiler.analysis.constraint;

import pascal.netx.compiler.model.instance.Instance;
import pascal.netx.compiler.model.instance.type.InstanceType;

public abstract sealed class BinaryConstraint implements DataConstraint permits EqualTypeConstraint {
    protected final Instance lhs, rhs;

    protected BinaryConstraint(Instance lhs, Instance rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Instance getLhs() {
        return lhs;
    }

    public Instance getRhs() {
        return rhs;
    }

    public abstract InstanceType expectedLhs();

    public abstract InstanceType expectedRhs();
}
