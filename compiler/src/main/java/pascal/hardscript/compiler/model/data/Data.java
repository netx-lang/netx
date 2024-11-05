package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.ast.SourceLoc;
import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.scope.ComponentScope;
import pascal.hardscript.compiler.model.scope.Scope;

public sealed interface Data extends Entity permits NonValue, Value {
    Scope getParent();

    DataType getType();

    void setType(DataType type);

    Identifier getIdentifier();

    default SourceLoc getSourceLoc() {
        return getIdentifier().getSourceLoc();
    }

    default int getDepth() {
        return getUpperComponent().getDepth();
    }

    default ComponentScope getUpperComponent() {
        if (getParent() instanceof ComponentScope module) {
            return module;
        } else {
            return getParent().getUpperComponent();
        }
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }

    default boolean isBuiltin() {
        return (this instanceof NonValue) && getIdentifier().string().startsWith("$");
    }

    interface Factory<T extends Data> extends Entity {
        T create(Identifier id);
    }
}
