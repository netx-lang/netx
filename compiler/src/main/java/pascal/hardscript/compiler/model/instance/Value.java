package pascal.hardscript.compiler.model.instance;

import pascal.hardscript.compiler.model.instance.type.InstanceType;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BitVector;

public sealed interface Value extends Instance permits BooleanValue, IntegerValue {
    @Override
    default void setType(InstanceType type) {
        throw new ShallNotReachHereError();
    }

    @Override
    default boolean isBuiltin() {
        return false;
    }

    BitVector toBitVector();
}
