package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

public sealed interface Value extends Instance permits BooleanValue, IntegerValue {
    @Override
    default Scope getParent() {
        throw new ShallNotReachHereError();
    }

    @Override
    default void setType(InstanceType type) {
        throw new ShallNotReachHereError();
    }

    @Override
    default Identifier getIdentifier() {
        throw new ShallNotReachHereError();
    }
}
