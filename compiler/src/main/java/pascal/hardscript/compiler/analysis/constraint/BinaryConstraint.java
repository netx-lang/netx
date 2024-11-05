package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

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
