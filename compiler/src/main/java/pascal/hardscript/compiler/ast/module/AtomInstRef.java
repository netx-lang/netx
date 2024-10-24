package pascal.hardscript.compiler.ast.module;

import pascal.hardscript.compiler.ast.Symbol;

public record AtomInstRef(Symbol id) implements InstRef {

    public static AtomInstRef of(Symbol id) {
        return new AtomInstRef(id);
    }
}
