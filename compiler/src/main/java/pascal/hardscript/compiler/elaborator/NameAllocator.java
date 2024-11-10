package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.component.ComponentAtom;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

public final class NameAllocator {

    public Identifier allocateFor(ComponentAtom type) {
        var identifier = type.getDefinition().getIdentifier();
        return allocate(identifier.name(), identifier.getSourceLoc());
    }

    public Identifier allocateFor(BuiltinAtom type) {
        return allocate(type.getBuiltin().name(), type.getSourceLoc());
    }

    private Identifier allocate(String name, SourceLoc sourceLoc) {
        return Identifier.of(name).setSourceLoc(sourceLoc);
    }

}
