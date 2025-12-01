package pascal.netx.compiler.backend.analysis.model;

import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.signal.Value;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.Stack;

/**
 * RedundantElimination is an analysis that removes redundant components from the design.
 * It specifically targets built-in components like CAST and CONCAT that can be simplified.
 */
public final class RedundantElimination extends ModelAnalysis {
    public RedundantElimination(PackageScope pack) {
        super(pack);
    }

    @Override
    public void analyze() {
        Stack<ComponentScope> stack = new Stack<>();
        stack.push(pack.getTop());
        while (!stack.isEmpty()) {
            ComponentScope scope = stack.pop();
            var toRemove = scope.getChildren().stream()
                    .map(ComponentScope.class::cast)
                    .filter(this::isRedundant).toList();
            for (var child : toRemove) {
                scope.getBinding().bind(child.getBinding());
                scope.getChildren().remove(child);
            }
            scope.getChildren().removeIf(child -> isRedundant((ComponentScope) child));
            scope.getChildren().forEach(child -> stack.push((ComponentScope) child));
        }
    }

    private boolean isRedundant(ComponentScope scope) {
        if (scope.isBuiltin()) {
            var builtinRef = BuiltinComponent.of(scope.getIdentifier());
            return switch (builtinRef) {
                case UCAST, CAST -> isRedundantCast(scope);
                case CONCAT -> isRedundantConcat(scope);
                case SPLIT, DUP -> true;
                default -> false;
            };
        }
        return false;
    }

    private boolean isRedundantCast(ComponentScope scope) {
        var lhs = scope.getOutputs().getFirst();
        var rhs = scope.getInputs().getFirst();
        boolean isRedundant = lhs.equals(rhs) || lhs instanceof Value;
        if (isRedundant) {
            return true;
        } else {
            if (lhs.getType().equals(rhs.getType())) {
                scope.setIdentifier(BuiltinComponent.TO.asID());
            }
            return false;
        }
    }

    private boolean isRedundantConcat(ComponentScope scope) {
        if (scope.getInputs().size() == 1) {
            var lhs = scope.getOutputs().getFirst();
            var rhs = scope.getInputs().getFirst();
            boolean isRedundant = lhs.equals(rhs) || lhs instanceof Value;
            if (isRedundant) {
                return true;
            } else {
                scope.setIdentifier(BuiltinComponent.TO.asID());
                return false;
            }
        }
        return false;
    }
}
