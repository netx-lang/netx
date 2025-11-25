package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.variable.Variable;

public final class VariableRef extends PrimaryExpr {
    private final Variable variable;

    private VariableRef(Variable variable) {
        this.variable = variable;
    }

    public static VariableRef of(Variable source) {
        return new VariableRef(source);
    }

    public Variable getInstance() {
        return variable;
    }

    @Override
    public String toString() {
        if (attrBindList != null) {
            return variable.toString() + attrBindList.toString();
        }
        return variable.toString();
    }
}
