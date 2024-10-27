package pascal.hardscript.compiler.model;

import pascal.hardscript.compiler.ast.Identifier;

import java.util.ArrayList;
import java.util.List;

public record ParameteriezedEntity(Identifier identifier, List<Entity> parameters) implements Entity {

    public static ParameteriezedEntity of(Identifier identifier) {
        return new ParameteriezedEntity(identifier, new ArrayList<>());
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public void addParameter(Entity value) {
        parameters.add(value);
    }
}
