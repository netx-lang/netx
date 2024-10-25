package pascal.hardscript.compiler.model.scope;

public final class ListScope extends Scope {
    private final Scope parent;

    public ListScope(Scope parent) {
        this.parent = parent;
    }
}
