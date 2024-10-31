package pascal.hardscript.compiler.model.value;

import pascal.hardscript.compiler.model.Entity;

public sealed interface Value extends Entity permits BooleanValue, IntegerValue {
}
