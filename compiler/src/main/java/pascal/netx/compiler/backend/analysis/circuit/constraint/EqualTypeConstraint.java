package pascal.netx.compiler.backend.analysis.circuit.constraint;

import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.model.type.Type;

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
        return "Type mismatch:\n"
                + WireConstraint.describeSignalBlock("lhs", lhs)
                + "\n"
                + WireConstraint.describeSignalBlock("rhs", rhs);
    }
}
