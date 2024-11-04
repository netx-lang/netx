package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.model.component.AtomType;

public final class NameAllocator {

    public Identifier allocateFor(AtomType type) {
        return allocate(type.identifier().string(), type.identifier().getSourceLoc());
    }

    private Identifier allocate(String name, SourceLoc sourceLoc) {
        return Identifier.of(name).setSourceLoc(sourceLoc);
    }

}
