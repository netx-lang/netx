package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.List;

public final class BuiltinAtom extends AtomType {
    private final BuiltinComponent builtin;
    private final SourceLoc sourceLoc;

    public BuiltinAtom(BuiltinComponent builtin, SourceLoc sourceLoc,
                       List<Entity> parameters) {
        this.builtin = builtin;
        this.sourceLoc = sourceLoc;
        setParameters(parameters);
    }

    public static BuiltinAtom of(BuiltinComponent builtin, SourceLoc sourceLoc, List<Entity> parameters) {
        return new BuiltinAtom(builtin, sourceLoc, parameters);
    }

    public BuiltinComponent getBuiltin() {
        return builtin;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }
}
