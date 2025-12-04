package pascal.netx.compiler.backend.analysis.circuit.constraint;

import pascal.netx.compiler.model.circuit.signal.Signal;
import pascal.netx.compiler.model.type.BitType;

import java.util.List;

public record ConcatConstraint(Signal lhs, List<Signal> rhs) implements WireConstraint {
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
