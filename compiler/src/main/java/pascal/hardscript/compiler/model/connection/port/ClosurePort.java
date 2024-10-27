package pascal.hardscript.compiler.model.connection.port;

public record ClosurePort(CommonPort lhs, CommonPort rhs) implements Port {
    public static ClosurePort of(CommonPort lhs, CommonPort rhs) {
        return new ClosurePort(lhs, rhs);
    }
}
