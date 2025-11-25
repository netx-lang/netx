package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.scope.binding.AttrBindList;

public sealed abstract class PrimaryExpr implements Expr permits BuiltinRef, ComponentRef, ImportRef, VariableRef {
    protected AttrBindList attrBindList;

    public AttrBindList getArgList() {
        return attrBindList;
    }

    public void setArgList(AttrBindList binding) {
        this.attrBindList = binding;
    }
}
