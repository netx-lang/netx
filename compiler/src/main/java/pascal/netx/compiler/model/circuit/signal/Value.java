package pascal.netx.compiler.model.circuit.signal;

import pascal.netx.compiler.model.type.Type;
import pascal.netx.util.exception.ShallNotReachHereError;
import pascal.netx.util.lang.BitVector;
import pascal.netx.util.lang.SourceLoc;

public sealed interface Value extends Signal permits VectorValue, BooleanValue {
    @Override
    default void setType(Type type) {
        throw new ShallNotReachHereError();
    }

    @Override
    default boolean isBuiltin() {
        return false;
    }

    @Override
    default SourceLoc getSourceLoc() {
        return SourceLoc.none();
    }

    BitVector toBitVector();
}
