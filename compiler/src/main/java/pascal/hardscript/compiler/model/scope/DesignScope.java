package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.elaborator.BuiltInComponentFactory;
import pascal.hardscript.compiler.model.component.BuiltinAtom;
import pascal.hardscript.compiler.model.component.ComponentAtom;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.BuiltinComponent;
import pascal.hardscript.util.lang.Identifier;

import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DesignScope extends Scope {
    private ComponentScope top;
    private final Supplier<Integer> dataIndexer;
    private final BuiltInComponentFactory factory = new BuiltInComponentFactory(this);

    DesignScope() {
        super(null);
        this.dataIndexer = new AtomicInteger(0)::incrementAndGet;;
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

    public ComponentScope instantiateTop(Identifier identifier) {
        if (this.top != null) {
            throw new ShallNotReachHereError();
        }
        this.top = instantiateComponentScope(identifier);
        return top;
    }

    public ComponentScope getTop() {
        return top;
    }

    public Supplier<Integer> getDataIndexer() {
        return dataIndexer;
    }

    public BuiltInComponentFactory getBuiltinFactory() {
        return factory;
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
