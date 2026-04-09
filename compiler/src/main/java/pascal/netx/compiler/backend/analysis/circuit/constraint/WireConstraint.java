package pascal.netx.compiler.backend.analysis.circuit.constraint;

import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.signal.Data;
import pascal.netx.compiler.model.circuit.signal.Signal;

import java.util.LinkedList;
import java.util.List;

public sealed interface WireConstraint permits BinaryConstraint, ConcatConstraint, UnaryConstraint {

    boolean holds();

    String error();

    static String describeSignalBlock(String label, Signal signal) {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(label).append(": ").append(signal);
        if (signal instanceof Data data) {
            for (var scopeName : describeParentScopes(data.getParent())) {
                builder.append("\n")
                        .append("      in ")
                        .append(scopeName);
            }
        }
        return builder.toString();
    }

    static String describeSignalListBlock(String label, List<? extends Signal> signals) {
        StringBuilder builder = new StringBuilder();
        builder.append("  ").append(label).append(":");
        for (var signal : signals) {
            builder.append("\n")
                    .append(describeSignalBlock("-", signal));
        }
        return builder.toString();
    }

    private static List<String> describeParentScopes(ComponentScope scope) {
        List<String> scopes = new LinkedList<>();
        for (var current = scope; current != null; ) {
            scopes.addLast(current.getIdentifier().name() + " (instantiated at " + current.getSourceLoc() + ")");
            current = current.getParent() instanceof ComponentScope parent ? parent : null;
        }
        return scopes;
    }
}
