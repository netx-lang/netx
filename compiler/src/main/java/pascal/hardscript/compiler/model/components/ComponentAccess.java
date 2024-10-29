package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.components.selector.Selector;
import pascal.hardscript.compiler.model.scope.Scope;

public record ComponentAccess(Component base, Selector selector) implements Component {

    public static ComponentAccess of(Component base, Selector selector) {
        return new ComponentAccess(base, selector);
    }

    @Override
    public Identifier getIdentifier() {
        return base.getIdentifier();
    }

    @Override
    public Scope getParent() {
        // FIXME: check
        return base.getParent();
    }
}
