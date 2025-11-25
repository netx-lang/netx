package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.circuit.variable.Variable;
import pascal.netx.compiler.circuit.variable.type.Type;

public abstract sealed class BinaryConstraint implements DataConstraint permits EqualTypeConstraint {
    protected final Variable lhs, rhs;

    protected BinaryConstraint(Variable lhs, Variable rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Variable getLhs() {
        return lhs;
    }

    public Variable getRhs() {
        return rhs;
    }

    public abstract Type expectedLhs();

    public abstract Type expectedRhs();
}
