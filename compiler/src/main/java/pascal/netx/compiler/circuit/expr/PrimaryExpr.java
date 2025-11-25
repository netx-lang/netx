package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.scope.binding.ArgBindList;

public sealed abstract class PrimaryExpr implements Expr permits BuiltinRef, ComponentRef, ImportRef, VariableRef {
    protected ArgBindList argBindList;

    public ArgBindList getArgList() {
        return argBindList;
    }

    public void setArgList(ArgBindList binding) {
        this.argBindList = binding;
    }
}
