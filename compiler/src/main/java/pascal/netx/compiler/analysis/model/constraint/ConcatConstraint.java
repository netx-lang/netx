package pascal.netx.compiler.analysis.model.constraint;

import pascal.netx.compiler.circuit.variable.Variable;
import pascal.netx.compiler.circuit.variable.type.BitType;

import java.util.List;

public record ConcatConstraint(Variable lhs, List<Variable> rhs) implements DataConstraint {
    @Override
    public boolean holds() {
        int length = 0;
        for (var element : rhs) {
            if (element.getType() instanceof BitType(int width)) {
                length += width;
            } else {
                return false;
            }
        }
        return lhs.getType() instanceof BitType(int width) && width == length;
    }

    @Override
    public String error() {
        return String.format("Type mismatch: %s and %s", lhs, rhs);
    }
}
