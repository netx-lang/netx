package pascal.netx.compiler.backend.analysis.circuit;

import pascal.netx.compiler.model.circuit.scope.ComponentScope;
import pascal.netx.compiler.model.circuit.scope.PackageScope;
import pascal.netx.compiler.model.circuit.scope.binding.Binding;

import java.util.Stack;

public final class BindingFlattener extends CircuitAnalysis {
    public BindingFlattener(PackageScope pack) {
        super(pack);
    }

    @Override
    public void analyze() {
        pack.forEachSubComponent(this::flattenBindingIn);
    }

    private void flattenBindingIn(ComponentScope scope) {
        Stack<Binding> stack = new Stack<>();
        while (scope.getBindingCount() > 1) {
            stack.push(scope.getTopBinding());
            scope.restoreBindings();
        }

        var base = scope.getBinding();
        while (!stack.isEmpty()) {
            base.bind(stack.pop());
        }
    }
}
