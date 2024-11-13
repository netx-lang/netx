package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.SourceLoc;

import java.util.List;

public final class BuiltinAtom extends AtomType {
    private final BuiltinComponent builtin;
    private SourceLoc sourceLoc;

    public BuiltinAtom(BuiltinComponent builtin, SourceLoc sourceLoc, List<Entity> parameters) {
        this.builtin = builtin;
        this.sourceLoc = sourceLoc;
        this.parameters = parameters;
    }

    public static BuiltinAtom of(BuiltinComponent builtin, SourceLoc loc, List<Entity> parameters) {
        return new BuiltinAtom(builtin, loc, parameters);
    }

    public static BuiltinAtom of(BuiltinComponent builtin, List<Entity> parameters) {
        return new BuiltinAtom(builtin, null, parameters);
    }

    public static BuiltinAtom of(BuiltinComponent builtin) {
        return new BuiltinAtom(builtin, null, List.of());
    }

    public BuiltinComponent getBuiltin() {
        return builtin;
    }

    public void setSourceLoc(SourceLoc sourceLoc) {
        this.sourceLoc = sourceLoc;
    }

    public SourceLoc getSourceLoc() {
        return sourceLoc;
    }

    @Override
    public AtomType copyOf() {
        return BuiltinAtom.of(builtin, sourceLoc, getParameters());
    }
}
