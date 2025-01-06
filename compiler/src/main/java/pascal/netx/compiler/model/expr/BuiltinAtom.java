package pascal.netx.compiler.model.expr;

import pascal.netx.compiler.model.Entity;
import pascal.netx.util.lang.BuiltinComponent;
import pascal.netx.util.lang.SourceLoc;

import java.util.List;

public final class BuiltinAtom extends AtomExpr {
    private final BuiltinComponent builtin;
    private final SourceLoc sourceLoc;

    private BuiltinAtom(BuiltinComponent builtin, SourceLoc sourceLoc, List<Entity> parameters) {
        this.builtin = builtin;
        this.sourceLoc = sourceLoc;
        this.parameters = parameters;
    }

    public static BuiltinAtom of(BuiltinComponent builtin, SourceLoc loc, List<Entity> parameters) {
        return new BuiltinAtom(builtin, loc, parameters);
    }

    public static BuiltinAtom of(BuiltinComponent builtin, SourceLoc loc) {
        return new BuiltinAtom(builtin, loc, List.of());
    }

    public BuiltinComponent getBuiltin() {
        return builtin;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
