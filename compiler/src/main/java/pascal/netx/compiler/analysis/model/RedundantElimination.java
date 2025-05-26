package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.model.scope.ComponentScope;
import pascal.netx.compiler.model.scope.PackageScope;
import pascal.netx.compiler.model.variable.Value;
import pascal.netx.util.lang.BuiltinComponent;

import java.util.Stack;

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
            scope.getChildren().removeIf(child -> isRedundant((ComponentScope) child));
            scope.getChildren().forEach(child -> stack.push((ComponentScope) child));
        }
    }

    private boolean isRedundant(ComponentScope scope) {
        if (scope.isBuiltin()) {
            var builtinRef = BuiltinComponent.of(scope.getIdentifier());
            return switch (builtinRef) {
                case CAST -> isRedundantCast(scope);
                case CONCAT -> isRedundantConcat(scope);
                default -> false;
            };
        }
        return false;
    }

    private boolean isRedundantCast(ComponentScope scope) {
        var lhs = scope.getOutputs().getFirst();
        var rhs = scope.getInputs().getFirst();
        return lhs.equals(rhs) || lhs instanceof Value;
    }

    private boolean isRedundantConcat(ComponentScope scope) {
        if (scope.getInputs().size() == 1) {
            var lhs = scope.getOutputs().getFirst();
            var rhs = scope.getInputs().getFirst();
            return lhs.equals(rhs) || lhs instanceof Value;
        }
        return false;
    }
}
