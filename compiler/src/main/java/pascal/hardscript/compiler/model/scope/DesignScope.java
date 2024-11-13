package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.component.ComponentAtom;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.Stack;
import java.util.function.Consumer;

public final class DesignScope extends Scope {
    private ComponentScope top;

    DesignScope() {
        super(null);
    }

    public static DesignScope newDesign(DesignNode node) {
        var scope = new DesignScope();
        for (var builtin : BuiltinComponent.values()) {
            scope.bind(Identifier.of(builtin.name()), BuiltinAtom.of(builtin));
        }
        var evaluator = scope.getEvaluator();
        for (var binding : node.getBindings()) {
            scope.bind(
                    evaluator.getIdentifierFrom(binding.getHead()),
                    evaluator.evaluate(binding.getExpr()));
        }
        for (var component : node.getComponents()) {
            scope.bind(component.getIdentifier(), ComponentAtom.of(component));
        }
        return scope;
    }

    public void setTop(ComponentScope top) {
        this.top = top;
    }

    public ComponentScope getTop() {
        return top;
    }

    public void forEachScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        stack.push(getTop());
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
