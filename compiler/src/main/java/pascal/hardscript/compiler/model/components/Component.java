package pascal.hardscript.compiler.model.components;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ModuleScope;
import pascal.hardscript.compiler.model.scope.Scope;

import java.util.Objects;

public sealed interface Component extends Entity permits Clock, ComponentAccess, Register, Wire {
    Scope getParent();

    default ModuleScope getModuleParent() {
        var parent = getParent();
        return Objects.requireNonNullElse(parent.getUpperModule(), (ModuleScope) parent);
    }

    Identifier getIdentifier();

    interface Factory<T extends Component> extends Entity {
        T create(Identifier id);
    }

    default boolean isBuiltin() {
        return getIdentifier().string().startsWith("$");
    }

    default boolean isUserDefined() {
        return !isBuiltin() && !(this instanceof ComponentAccess);
    }
}
