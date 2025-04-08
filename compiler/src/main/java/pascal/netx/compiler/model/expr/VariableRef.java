package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.variable.Variable;
import pascal.netx.util.lang.SourceLoc;

public final class VariableRef extends PrimaryExpr {
    private final Variable variable;
    private final SourceLoc sourceLoc;

    private VariableRef(Variable variable, SourceLoc sourceLoc) {
        this.variable = variable;
        this.sourceLoc = sourceLoc;
    }

    public static VariableRef of(Variable source, SourceLoc sourceLoc) {
        return new VariableRef(source, sourceLoc);
    }

    public Variable getInstance() {
        return variable;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    @Override
    public String toString() {
        if (argBindList != null) {
            return variable.toString() + argBindList.toString();
        }
        return variable.toString();
    }
}
