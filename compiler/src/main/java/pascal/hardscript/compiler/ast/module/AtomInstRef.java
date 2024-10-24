package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.Identifier;

public record AtomInstRef(Identifier id) implements InstRef {

    public static AtomInstRef of(Identifier id) {
        return new AtomInstRef(id);
    }
}
