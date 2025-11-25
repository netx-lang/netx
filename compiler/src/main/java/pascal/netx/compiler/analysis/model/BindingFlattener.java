package pascal.netx.compiler.analysis.model;

import pascal.netx.compiler.circuit.scope.ComponentScope;
import pascal.netx.compiler.circuit.scope.PackageScope;
import pascal.netx.compiler.circuit.scope.binding.Binding;

import java.util.Stack;

public final class BindingFlattener extends ModelAnalysis {
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
