package pascal.hardscript.compiler.model.connection;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.components.Component;

import java.util.List;

public record Port(List<Component> from, List<Component> to) implements Entity {
    public static Port of(List<Component> from, List<Component> to) {
        return new Port(from, to);
    }
}
