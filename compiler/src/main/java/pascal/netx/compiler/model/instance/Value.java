package pascal.netx.compiler.model.instance;

import pascal.netx.compiler.model.instance.type.InstanceType;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;

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
