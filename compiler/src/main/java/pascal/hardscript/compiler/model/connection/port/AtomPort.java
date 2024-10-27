package pascal.hardscript.compiler.model.connection.port;

import pascal.hardscript.compiler.model.components.Component;

import java.util.List;

public record AtomPort(List<Component> components) implements CommonPort {
    public static AtomPort from(List<Component> components) {
        return new AtomPort(components);
    }
}
