package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.meta.Value;

import java.util.HashMap;
import java.util.Map;

public final class ModuleScope extends Scope {
    private final Identifier identifier;
    private final Map<Identifier, Value> paramBindings = new HashMap<>();

    ModuleScope(Scope parent, Identifier identifier) {
        super(parent);
        this.identifier = identifier;
    }
}
