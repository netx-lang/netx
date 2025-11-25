package pascal.netx.compiler.circuit.variable;

import pascal.netx.compiler.circuit.variable.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;

public sealed interface Value extends Variable permits BooleanValue, IntegerValue {
    @Override
    default void setType(Type type) {
        throw new ShallNotReachHereError();
    }

    @Override
    default boolean isBuiltin() {
        return false;
    }

    BitVector toBitVector();
}
