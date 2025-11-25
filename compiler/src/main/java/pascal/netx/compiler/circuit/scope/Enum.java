package pascal.netx.compiler.circuit.scope;

import pascal.netx.compiler.circuit.variable.BitVectorValue;
import pascal.netx.util.lang.Identifier;

import java.util.Map;

public record Enum(Identifier name, int width, Map<Identifier, BitVectorValue> fields) {

}
