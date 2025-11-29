package pascal.netx.compiler.circuit.expr;

import pascal.netx.compiler.circuit.scope.binding.AttrBindList;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.SourceLoc;

public final class BuiltinRef extends PrimaryExpr implements InstantiationRef {
    private final BuiltinComponent builtin;
    private final SourceLoc sourceLoc;

    private BuiltinRef(BuiltinComponent builtin, SourceLoc sourceLoc, AttrBindList attrBindList) {
        this.builtin = builtin;
        this.sourceLoc = sourceLoc;
        this.attrBindList = attrBindList;
    }

    public static BuiltinRef of(BuiltinComponent builtin, SourceLoc loc) {
        return new BuiltinRef(builtin, loc, AttrBindList.ofEmpty());
    }

    public BuiltinComponent getBuiltin() {
        return builtin;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    @Override
    public String toString() {
        if (hasAttrBind()) {
            return builtin.toString() + attrBindList;
        }
        return builtin.toString();
    }
}
