package pascal.hardscript.compiler.model.data;

public sealed interface NonValue extends Data permits Clock, DataAccess, Wire {
}
