package pascal.hardscript.compiler.model.connection;

import pascal.hardscript.compiler.model.connection.instantiation.InstanceType;
import pascal.hardscript.compiler.model.connection.port.Port;

public record Connection(InstanceType type, Port port) {
    public static Connection of(InstanceType type, Port port) {
        return new Connection(type, port);
    }
}
