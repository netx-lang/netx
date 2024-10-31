package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ModuleScope;
import pascal.hardscript.compiler.model.scope.Scope;

public sealed interface Component extends Entity permits Clock, ComponentAccess, Wire {
    Scope getParent();

    default int getDepth() {
        if (getParent() instanceof ModuleScope module) {
            return module.getDepth();
        } else {
            return getParent().getUpperModule().getDepth();
        }
    }

    int getWidth();

    Identifier getIdentifier();

    interface Factory<T extends Component> extends Entity {
        T create(Identifier id);
    }

    default boolean isBuiltin() {
        return getIdentifier().string().startsWith("$");
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }
}
