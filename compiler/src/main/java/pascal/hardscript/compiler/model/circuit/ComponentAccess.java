package pascal.hardscript.compiler.model.circuit;

import pascal.hardscript.compiler.model.circuit.selector.Selector;

public final class ComponentAccess extends Component {
    private final Component base;
    private final Selector selector;

    public ComponentAccess(Component base, Selector selector) {
        this.base = base;
        this.selector = selector;
    }

    public Component base() {
        return base;
    }

    public Selector selector() {
        return selector;
    }

    public static ComponentAccess of(Component base, Selector selector) {
        return new ComponentAccess(base, selector);
    }
}
