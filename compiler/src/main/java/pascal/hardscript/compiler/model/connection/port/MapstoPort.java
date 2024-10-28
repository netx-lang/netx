package pascal.hardscript.compiler.model.connection.port;

public record MapstoPort(CommonPort from, CommonPort to) implements CommonPort {
    public static MapstoPort of(CommonPort lhs, CommonPort rhs) {
        return new MapstoPort(lhs, rhs);
    }
}
