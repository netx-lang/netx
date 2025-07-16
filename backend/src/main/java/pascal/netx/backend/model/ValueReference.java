package pascal.netx.backend.model;

public record ValueReference(String constant) implements Reference {
    @Override
    public String dump() {
        return constant;
    }
}
