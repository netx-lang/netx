package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.model.components.selector.Selector;

public record ComponentAccess(Component base, Selector selector) implements Component {

    public static ComponentAccess of(Component base, Selector selector) {
        return new ComponentAccess(base, selector);
    }
}
