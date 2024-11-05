package pascal.hardscript.compiler.model.data;

import pascal.hardscript.compiler.ast.Identifier;
import pascal.hardscript.compiler.model.scope.Scope;
import pascal.hardscript.util.exception.ShallNotReachHereError;

public sealed interface Value extends Data permits BooleanValue, IntegerValue {
    @Override
    default Scope getParent() {
        throw new ShallNotReachHereError();
    }

    @Override
    default void setType(DataType type) {
        throw new ShallNotReachHereError();
    }

    @Override
    default Identifier getIdentifier() {
        throw new ShallNotReachHereError();
    }
}
