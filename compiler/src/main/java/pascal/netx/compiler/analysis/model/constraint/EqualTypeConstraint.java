package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.Type;

public final class EqualTypeConstraint extends BinaryConstraint {
    private EqualTypeConstraint(Variable lhs, Variable rhs) {
        super(lhs, rhs);
    }

    public static EqualTypeConstraint of(Variable lhs, Variable rhs) {
        return new EqualTypeConstraint(lhs, rhs);
    }

    @Override
    public Type expectedLhs() {
        return rhs.getType();
    }

    @Override
    public Type expectedRhs() {
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
