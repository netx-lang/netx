package pascal.hardscript.compiler.model.scope;

import pascal.hardscript.compiler.ast.DesignNode;
import pascal.hardscript.compiler.ast.component.ComponentNode;
import pascal.hardscript.compiler.elaborator.BuiltInComponentFactory;
import pascal.hardscript.util.exception.ShallNotReachHereError;
import pascal.hardscript.util.lang.Identifier;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Stack;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class DesignScope extends Scope {
    private final Supplier<Integer> dataIndexer;
    private final Map<Identifier, ComponentNode> definitions = new HashMap<>();
    private final BuiltInComponentFactory factory;
    private ComponentScope top;

    DesignScope() {
        super(null);
        this.dataIndexer = new AtomicInteger(0)::incrementAndGet;
        this.factory = new BuiltInComponentFactory(dataIndexer);
    }

    public static DesignScope newDesign(DesignNode node) {
        var scope = new DesignScope();
        var evaluator = scope.getEvaluator();
        for (var binding : node.getBindings()) {
            scope.bind(evaluator.evaluateBinding(binding));
        }
        for (var component : node.getComponents()) {
            scope.definitions.put(component.getIdentifier(), component);
        }
        return scope;
    }

    public ComponentScope instantiateTop(Identifier identifier) {
        if (this.top != null) {
            throw new ShallNotReachHereError();
        }
        this.top = instantiateComponentScope(identifier);
        return this.top;
    }

    @Override
    public ComponentScope instantiateComponentScope(Identifier identifier) {
        var child = new ComponentScope(this, identifier);
        addChild(child);
        return child;
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

    public boolean hasDefinition(Identifier id) {
        return definitions.containsKey(id);
    }

    public ComponentNode getDefinition(Identifier id) {
        return Objects.requireNonNull(definitions.get(id));
    }

    public void forEachSubScope(Consumer<Scope> consumer) {
        Stack<Scope> stack = new Stack<>();
        stack.push(getTop());
        while (!stack.isEmpty()) {
            Scope scope = stack.pop();
            consumer.accept(scope);
            stack.addAll(scope.getChildren());
        }
    }
}
