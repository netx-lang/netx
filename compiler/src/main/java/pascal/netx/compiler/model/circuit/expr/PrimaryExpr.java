package pascal.netx.compiler.model.circuit.expr;

import pascal.netx.compiler.model.circuit.scope.binding.AttrBindList;

public sealed abstract class PrimaryExpr implements Expr permits BuiltinRef, ComponentRef, ImportRef, SignalRef {
    protected AttrBindList attrBindList;

    public AttrBindList getArgList() {
        return attrBindList;
    }

    public void setArgList(AttrBindList binding) {
        this.attrBindList = binding;
    }

    /** Call arguments belong to an invocation, never to a captured reference. */
    public PrimaryExpr withArgList(AttrBindList binding) {
        PrimaryExpr result = switch (this) {
            case BuiltinRef ref -> BuiltinRef.of(ref.getBuiltin(), ref.getSourceLoc());
            case ComponentRef ref -> ComponentRef.of(ref.getDefinition(), ref.getIdentifier());
            case ImportRef ref -> ref.copy(ref.getSourceLoc());
            case SignalRef ref -> SignalRef.of(ref.getInstance(), ref.getSourceLoc());
        };
        result.setArgList(binding);
        return result;
    }

    protected boolean hasAttrBind() {
        return attrBindList != null && !attrBindList.isEmpty();
    }
}
