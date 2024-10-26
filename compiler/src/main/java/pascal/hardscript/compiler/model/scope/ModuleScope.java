package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;

public final class ModuleScope extends Scope {
    private final Identifier identifier;

    ModuleScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
    }
}
