package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.meta.MetaVariable;

import java.util.HashMap;
import java.util.Map;

public final class ModuleScope extends Scope {
    private final Map<Identifier, MetaVariable> paramBindings = new HashMap<>();

    private ModuleScope(Scope parent) {
        super(parent);
    }
}
