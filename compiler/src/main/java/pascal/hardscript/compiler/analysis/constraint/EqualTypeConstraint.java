package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataType;

public final class EqualTypeConstraint extends BinaryConstraint {
    private EqualTypeConstraint(Data lhs, Data rhs) {
        super(lhs, rhs);
    }

    public static EqualTypeConstraint of(Data lhs, Data rhs) {
        return new EqualTypeConstraint(lhs, rhs);
    }

    @Override
    public DataType expectedLhs() {
        return rhs.getType();
    }

    @Override
    public DataType expectedRhs() {
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
