package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.data.Data;
import pascal.hardscript.compiler.model.data.DataType;

public abstract sealed class BinaryConstraint implements DataConstraint permits EqualTypeConstraint {
    protected final Data lhs, rhs;

    protected BinaryConstraint(Data lhs, Data rhs) {
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public Data getLhs() {
        return lhs;
    }

    public Data getRhs() {
        return rhs;
    }

    public abstract DataType expectedLhs();

    public abstract DataType expectedRhs();
}
