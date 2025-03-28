package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.Entity;
import pascal.netx.compiler.model.scope.binding.ArgBindList;

import java.util.List;

public sealed abstract class PrimaryExpr implements Expr permits BuiltinRef, ComponentRef, ImportRef, VariableRef {
    protected ArgBindList argBindList;

    public ArgBindList getArgList() {
        return argBindList;
    }

    public List<Entity> getArguments() {
        return argBindList.getArguments();
    }

    public void setArgList(ArgBindList binding) {
        this.argBindList = binding;
    }
}
