package pascal.netx.backend.optimization.model;

public sealed interface Vertex permits AbstractVertex {
    int index();

    int outWidth();

    String name();
}
