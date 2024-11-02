package pascal.hardscript.compiler.analysis.constraint;

import pascal.hardscript.compiler.model.data.Data;

public abstract sealed class UnaryConstraint implements DataConstraint permits ClockConstraint {
    protected final Data data;

    protected UnaryConstraint(Data data) {
        this.data = data;
    }

    public Data getData() {
        return data;
    }
}
