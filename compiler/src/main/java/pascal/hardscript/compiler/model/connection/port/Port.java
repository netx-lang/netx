package pascal.hardscript.compiler.model.connection.port;

import pascal.hardscript.compiler.model.Entity;

public sealed interface Port extends Entity permits ClosurePort, CommonPort {
}
