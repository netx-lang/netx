package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import java.util.HashMap;
import java.util.Map;

public abstract class Scope {
    private final Map<Identifier, Entity> binding = new HashMap<>();

}
