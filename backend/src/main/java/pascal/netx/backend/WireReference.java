package pascal.netx.backend;

public record WireReference(String name, Integer id, String selector) implements Reference {

    public boolean hasSelector() {
        return !selector.isEmpty();
    }

    public WireReference withoutSelector() {
        return new WireReference(name, id, "");
    }

    public WireReference withSelector(String selector) {
        return new WireReference(name, id, selector);
    }

    @Override
    public String dump() {
        return "_" + id + selector;
    }
}
