package pascal.hardscript.compiler.model.connection;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;

import java.util.List;

public record AtomType(Identifier identifier, List<Entity> parameters) implements InstanceType {

    public static AtomType of(Identifier identifier) {
        return new AtomType(identifier, List.of());
    }

    public static AtomType of(Identifier identifier, List<Entity> parameters) {
        return new AtomType(identifier, parameters);
    }
}
