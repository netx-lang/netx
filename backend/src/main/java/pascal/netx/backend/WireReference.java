package pascal.netx.backend;

public record WireReference(String name, Integer id, String selector) implements Reference {

    public boolean hasSelector() {
        return !selector.isEmpty();
    }

    public WireReference withoutSelector() {
        return new WireReference(name, id, "");
    }

    @Override
    public String dump() {
        return "_" + id + selector;
    }
}
