package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.Entity;
import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.instance.type.PolyType;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.lang.Identifier;
import pascal.hardscript.util.lang.SourceLoc;

public sealed interface Instance extends Entity permits Data, Value {
    Scope getParent();

    default boolean hasImplicitType() {
        return getType() instanceof PolyType;
    }

    InstanceType getType();

    void setType(InstanceType type);

    Identifier getIdentifier();

    default SourceLoc getSourceLoc() {
        return getIdentifier().getSourceLoc();
    }

    default int getDepth() {
        return getParent().getLeastUpperComponent().getDepth();
    }

    default boolean isUserDefined() {
        return !isBuiltin();
    }

    default boolean isBuiltin() {
        return (this instanceof Data) && getIdentifier().name().startsWith("$");
    }
}
