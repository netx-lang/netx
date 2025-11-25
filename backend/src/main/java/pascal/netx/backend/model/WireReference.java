package pascal.netx.backend.model;

public record WireReference(String name, Integer id, String selector) implements Reference {

    public boolean hasSelector() {
        return !selector.isEmpty();
    }

    @Override
    public String dump() {
        return "_" + id + selector;
    }
}
