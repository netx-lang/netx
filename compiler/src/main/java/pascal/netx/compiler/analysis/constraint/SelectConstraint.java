package pascal.netx.compiler.analysis.constraint;

import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.compiler.model.variable.type.BitType;

public final class SelectConstraint implements DataConstraint {
    private final Variable base;
    private final int msb, lsb;

    private SelectConstraint(Variable base, int msb, int lsb) {
        this.base = base;
        this.msb = msb;
        this.lsb = lsb;
    }

    public static SelectConstraint of(Variable base, int msb, int lsb) {
        return new SelectConstraint(base, msb, lsb);
    }

    @Override
    public boolean holds() {
        if (base.getType() instanceof BitType(var width)) {
            return 0 <= lsb && lsb < width && 0 <= msb && msb < width;
        }
        return false;
    }

    @Override
    public String error() {
        return String.format("Cannot access %s with [%d:%d]", base.getType(), msb, lsb);
    }
}
