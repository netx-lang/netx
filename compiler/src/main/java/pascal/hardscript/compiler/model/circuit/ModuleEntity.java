package pascal.hardscript.compiler.model.circuit;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.value.Value;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public final class ModuleEntity implements Entity {
    private final Identifier identifier;
    private final List<Value> parameters = new LinkedList<>();

    public ModuleEntity(Identifier identifier) {
        this.identifier = identifier;
    }

    public static ModuleEntity of(Identifier identifier) {
        return new ModuleEntity(identifier);
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public List<Value> getParameters() {
        return Collections.unmodifiableList(parameters);
    }

    public void addParameter(Value value) {
        parameters.add(value);
    }
}
