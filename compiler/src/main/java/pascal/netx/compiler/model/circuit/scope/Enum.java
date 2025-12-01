package pascal.netx.compiler.model.circuit.scope;

import pascal.netx.compiler.model.circuit.signal.VectorValue;
import pascal.netx.util.lang.Identifier;

import java.util.Map;

public record Enum(Identifier name, int width, Map<Identifier, VectorValue> fields) {

}
