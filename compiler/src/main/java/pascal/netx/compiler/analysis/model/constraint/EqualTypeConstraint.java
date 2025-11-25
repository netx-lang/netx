package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.circuit.signal.Signal;
import pascal.netx.compiler.circuit.signal.type.Type;

public final class EqualTypeConstraint extends BinaryConstraint {
    public EqualTypeConstraint(Signal lhs, Signal rhs) {
        super(lhs, rhs);
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
