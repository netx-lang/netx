package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.components.selector.Selector;
import pascal.hardscript.util.exception.ShallNotReachHereError;

public record ComponentAccess(Component base, Selector selector) implements Component {

    public static ComponentAccess of(Component base, Selector selector) {
        return new ComponentAccess(base, selector);
    }

    @Override
    public Identifier getIdentifier() {
        throw new ShallNotReachHereError();
    }
}
