package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.scope.binding.ArgBindList;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.SourceLoc;

public final class BuiltinRef extends PrimaryExpr {
    private final BuiltinComponent builtin;
    private final SourceLoc sourceLoc;

    private BuiltinRef(BuiltinComponent builtin, SourceLoc sourceLoc, ArgBindList argBindList) {
        this.builtin = builtin;
        this.sourceLoc = sourceLoc;
        this.argBindList = argBindList;
    }

    public static BuiltinRef of(BuiltinComponent builtin, SourceLoc loc) {
        return new BuiltinRef(builtin, loc, ArgBindList.ofEmpty());
    }

    public BuiltinComponent getBuiltin() {
        return builtin;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
