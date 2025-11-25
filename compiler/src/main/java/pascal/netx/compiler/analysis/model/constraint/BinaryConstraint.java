package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.type.Type;

public abstract sealed class BinaryConstraint implements WireConstraint permits EqualTypeConstraint {
    protected final Signal lhs, rhs;

    protected BinaryConstraint(Signal lhs, Signal rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Signal getLhs() {
        return lhs;
    }

    public Signal getRhs() {
        return rhs;
    }

    public abstract Type expectedLhs();

    public abstract Type expectedRhs();
}
