package pascal.netx.compiler.circuit.signal;

import pascal.netx.compiler.circuit.signal.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;

public sealed interface Value extends Signal permits VectorValue, BooleanValue {
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
