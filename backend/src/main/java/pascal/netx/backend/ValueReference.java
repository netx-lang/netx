package pascal.netx.backend;

public record ValueReference(String constant) implements Reference {
    @Override
    public String dump() {
        return constant;
    }
}
