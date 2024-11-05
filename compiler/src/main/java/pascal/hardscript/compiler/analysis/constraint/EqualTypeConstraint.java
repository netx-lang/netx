package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.instance.Instance;
import pascal.hardscript.compiler.model.instance.type.InstanceType;

public final class EqualTypeConstraint extends BinaryConstraint {
    private EqualTypeConstraint(Instance lhs, Instance rhs) {
        super(lhs, rhs);
    }

    public static EqualTypeConstraint of(Instance lhs, Instance rhs) {
        return new EqualTypeConstraint(lhs, rhs);
    }

    @Override
    public InstanceType expectedLhs() {
        return rhs.getType();
    }

    @Override
    public InstanceType expectedRhs() {
        return lhs.getType();
    }

    @Override
    public boolean holds() {
        return lhs.getType().equals(rhs.getType());
    }

    @Override
    public String error() {
        return String.format("Type mismatch: %s and %s", lhs, rhs);
    }
}
