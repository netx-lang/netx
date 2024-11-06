package pascal.hardscript.compiler.model.component;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.util.lang.Identifier;

import java.util.List;

public record AtomType(Identifier identifier, List<Entity> parameters) implements ComponentType {

    public static AtomType of(Identifier identifier) {
        return new AtomType(identifier, List.of());
    }

    public static AtomType of(Identifier identifier, List<Entity> parameters) {
        return new AtomType(identifier, parameters);
    }
}
