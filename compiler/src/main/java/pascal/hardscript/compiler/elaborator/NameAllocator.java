package pascal.hardscript.compiler.elaborator;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.model.connection.AtomType;

import java.util.HashMap;
import java.util.Map;

public final class NameAllocator {
    private final Map<String, Integer> count = new HashMap<>();

    public Identifier allocateFor(AtomType type) {
        return allocate(type.identifier().string(), type.identifier().getSourceLoc());
    }

    private Identifier allocate(String name, SourceLoc sourceLoc) {
        int index = countIdentifier(name);
        var result = Identifier.of(name + "_" + index);
        result.setSourceLoc(sourceLoc);
        return result;
    }

    private int countIdentifier(String name) {
        return count.compute(name, (_, c) -> c == null ? 1 : c + 1);
    }
}
