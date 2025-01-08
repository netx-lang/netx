package pascal.netx.compiler.model.scope;

import pascal.netx.compiler.model.variable.BitVectorValue;
import pascal.netx.util.lang.Identifier;

import java.util.Map;

public record Enum(Identifier name, int width, Map<Identifier, BitVectorValue> fields) {

}
